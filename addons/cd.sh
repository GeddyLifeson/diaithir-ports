cd /home/user/work-1a/addons/${1:-createdeco}
export JAVA_HOME=/home/user/srv/jdk-25.0.5+8
./gradlew --no-daemon --offline compileJava > /tmp/build_$1.log 2>&1
sed -i 's|/home/user/work-1a/addons/[a-z]*/src/main/java/||' /tmp/build_$1.log
grep -c "error:" /tmp/build_$1.log; grep BUILD /tmp/build_$1.log
