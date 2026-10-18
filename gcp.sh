#!/bin/bash
# One paste in Google Cloud Shell (signed in as the account with the $300 credit). Creates project diaithir-v1-279877749,
# lets GitHub Actions on GeddyLifeson/DiaithirRPG use it with no key (workload identity), adds the work bucket, asks for
# one L4 GPU of quota, and installs the hard stop: a $1 budget on cost after credits whose alert unlinks billing,
# so nothing is ever charged once the credit is gone. Safe to run again.
set -u
P=diaithir-v1-279877749 R=us-central1 REPO=GeddyLifeson/DiaithirRPG
BA=$(gcloud billing accounts list --filter=open=true --format='value(name)' | head -1)
gcloud projects describe $P >/dev/null 2>&1 || gcloud projects create $P
gcloud config set project $P
gcloud billing projects link $P --billing-account=$BA
N=$(gcloud projects describe $P --format='value(projectNumber)')
gcloud services enable compute.googleapis.com storage.googleapis.com iam.googleapis.com iamcredentials.googleapis.com \
  sts.googleapis.com cloudbilling.googleapis.com billingbudgets.googleapis.com pubsub.googleapis.com cloudquotas.googleapis.com \
  cloudfunctions.googleapis.com run.googleapis.com cloudbuild.googleapis.com eventarc.googleapis.com artifactregistry.googleapis.com
sleep 30   # the default compute service account appears a little after the API
SA=runner@$P.iam.gserviceaccount.com
gcloud iam service-accounts create runner 2>/dev/null
for r in compute.admin storage.admin iam.serviceAccountUser serviceusage.serviceUsageConsumer; do
  gcloud projects add-iam-policy-binding $P --member=serviceAccount:$SA --role=roles/$r --condition=None -q >/dev/null; done
gcloud iam workload-identity-pools create gh --location=global 2>/dev/null
gcloud iam workload-identity-pools providers create-oidc gh --location=global --workload-identity-pool=gh \
  --issuer-uri=https://token.actions.githubusercontent.com --attribute-condition="assertion.repository=='$REPO'" \
  --attribute-mapping=google.subject=assertion.sub,attribute.repository=assertion.repository 2>/dev/null
gcloud iam service-accounts add-iam-policy-binding $SA --role=roles/iam.workloadIdentityUser -q >/dev/null \
  --member=principalSet://iam.googleapis.com/projects/$N/locations/global/workloadIdentityPools/gh/attribute.repository/$REPO
gcloud storage buckets create gs://$P-work --location=$R 2>/dev/null
# hard stop: budget alert -> Pub/Sub -> function that unlinks billing when cost after credits reaches $1
gcloud pubsub topics create billing-stop 2>/dev/null
gcloud projects add-iam-policy-binding $P --member=serviceAccount:$N-compute@developer.gserviceaccount.com --role=roles/billing.projectManager --condition=None -q >/dev/null
for r in cloudbuild.builds.builder logging.logWriter artifactregistry.writer storage.objectViewer; do
  gcloud projects add-iam-policy-binding $P --member=serviceAccount:$N-compute@developer.gserviceaccount.com --role=roles/$r --condition=None -q >/dev/null; done
D=$(mktemp -d) && cd $D
printf 'functions-framework>=3\ngoogle-cloud-billing>=1\n' > requirements.txt
cat > main.py <<'EOF'
import base64, json, os
import functions_framework
from google.cloud import billing_v1

@functions_framework.cloud_event
def stop(e):
    d = json.loads(base64.b64decode(e.data["message"]["data"]))
    if d["costAmount"] >= d["budgetAmount"]:
        billing_v1.CloudBillingClient().update_project_billing_info(name=f"projects/{os.environ['P']}",
            project_billing_info=billing_v1.ProjectBillingInfo(billing_account_name=""))
EOF
gcloud functions deploy billing-stop --gen2 --runtime=python312 --region=$R --source=. --entry-point=stop \
  --trigger-topic=billing-stop --set-env-vars=P=$P -q
cd - >/dev/null
gcloud billing budgets list --billing-account=$BA --format='value(displayName)' | grep -qx diaithir-stop ||
  gcloud billing budgets create --billing-account=$BA --display-name=diaithir-stop --budget-amount=1USD \
    --filter-projects=projects/$P --credit-types-treatment=include-all-credits --threshold-rule=percent=1.0 \
    --notifications-rule-pubsub-topic=projects/$P/topics/billing-stop
gcloud beta quotas preferences create --service=compute.googleapis.com --quota-id=GPUS-ALL-REGIONS-per-project \
  --preferred-value=1 --project=$P --preference-id=gpus-all --justification="LLM batch jobs, one L4" -q 2>&1 | tail -1
G=$(gcloud compute project-info describe --format=json | python3 -c "import json,sys; print(next((int(q['limit']) for q in json.load(sys.stdin)['quotas'] if q['metric'] == 'GPUS_ALL_REGIONS'), 0))")
echo; echo "=========================================="
[ "$G" -gt 0 ] && echo "  GPUs ready: $G" || echo "  GPU quota still 0 (asked Google for 1; trial accounts must be upgraded first)"
echo "  DONE. Send Claude this number: $N"
echo "=========================================="
