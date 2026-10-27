package cn.leolezury.eternalstarlight.common.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record Chain(List<Segment> segments) {
	private static final String TAG_X = "x";
	private static final String TAG_Y = "y";
	private static final String TAG_Z = "z";
	private static final String TAG_LENGTH = "length";
	private static final String TAG_PITCH = "pitch";
	private static final String TAG_YAW = "yaw";
	private static final String TAG_SEGMENTS_SIZE = "segments_size";
	private static final String TAG_SEGMENT = "segment";

	public Chain(Vec3 position, int numSegments, float segmentLength) {
		this(new ArrayList<>());
		for (int i = 0; i < numSegments; i++) {
			segments().add(new Segment(position.add(0, numSegments - i - 1, 0), segmentLength));
		}
	}

	public void update(Vec3 targetPos, Vec3 attachPos, float rotationSpeed) {
		Vec3 rotationTarget = targetPos;
		Vec3 lastLowerPos = targetPos;
		for (int i = 0; i < segments().size(); i++) {
			Segment segment = segments().get(i);
			segment.rotateTowards(rotationTarget, rotationSpeed);
			segment.setUpperPosition(lastLowerPos);
			rotationTarget = segment.getUpperPosition();
			lastLowerPos = segment.getLowerPosition();
		}
		if (attachPos != null) {
			Vec3 offset = attachPos.subtract(lastLowerPos);
			for (Segment segment : segments()) {
				segment.setLowerPosition(segment.getLowerPosition().add(offset));
			}
		}
	}

	public Optional<Vec3> getEndPos() {
		return segments().isEmpty() ? Optional.empty() : Optional.of(segments().getFirst().getUpperPosition());
	}

	public void save(CompoundTag tag) {
		tag.putInt(TAG_SEGMENTS_SIZE, segments().size());
		for (int i = 0; i < segments().size(); i++) {
			Segment segment = segments().get(i);
			CompoundTag segmentTag = new CompoundTag();
			segment.save(segmentTag);
			tag.put(TAG_SEGMENT + i, segmentTag);
		}
	}

	public static Chain load(CompoundTag tag) {
		List<Segment> segments = new ArrayList<>();
		int segmentsSize = tag.getIntOr(TAG_SEGMENTS_SIZE, 0);
		for (int i = 0; i < segmentsSize; i++) {
			if (tag.contains(TAG_SEGMENT + i)) {
				CompoundTag segmentTag = tag.getCompoundOrEmpty(TAG_SEGMENT + i);
				Segment segment = Segment.load(segmentTag);
				segments.add(segment);
			}
		}
		return new Chain(segments);
	}

	public static class Segment {
		private final float length;

		public float getLength() {
			return length;
		}

		private Vec3 position;

		public Vec3 getLowerPosition() {
			return position;
		}

		public Vec3 getMiddlePosition() {
			return ESMathUtil.rotationToPosition(position, length / 2f, pitch, yaw);
		}

		public Vec3 getUpperPosition() {
			return ESMathUtil.rotationToPosition(position, length, pitch, yaw);
		}

		public void setLowerPosition(Vec3 position) {
			this.position = position;
		}

		public void setUpperPosition(Vec3 position) {
			this.position = position.add(getLowerPosition()).subtract(getUpperPosition());
		}

		private float pitch, yaw;

		public float getPitch() {
			return pitch;
		}

		public void setPitch(float pitch) {
			this.pitch = pitch;
		}

		public float getYaw() {
			return yaw;
		}

		public void setYaw(float yaw) {
			this.yaw = yaw;
		}

		public Segment(Vec3 position, float length) {
			this.position = position;
			this.length = length;
		}

		public Segment(Vec3 position, float length, float pitch, float yaw) {
			this(position, length);
			this.pitch = pitch;
			this.yaw = yaw;
		}

		public void rotateTowards(Vec3 target, float maxSpeed) {
			float wantedPitch = ESMathUtil.positionToPitch(position, target);
			float wantedYaw = ESMathUtil.positionToYaw(position, target);
			setPitch(Mth.approachDegrees(pitch, wantedPitch, maxSpeed));
			setYaw(Mth.approachDegrees(yaw, wantedYaw, maxSpeed));
		}

		public void save(CompoundTag tag) {
			tag.putFloat(TAG_X, (float) getLowerPosition().x);
			tag.putFloat(TAG_Y, (float) getLowerPosition().y);
			tag.putFloat(TAG_Z, (float) getLowerPosition().z);
			tag.putFloat(TAG_LENGTH, getLength());
			tag.putFloat(TAG_PITCH, getPitch());
			tag.putFloat(TAG_YAW, getYaw());
		}

		public static Segment load(CompoundTag tag) {
			Vec3 pos = new Vec3(tag.getFloatOr(TAG_X, 0F), tag.getFloatOr(TAG_Y, 0F), tag.getFloatOr(TAG_Z, 0F));
			float length = tag.getFloatOr(TAG_LENGTH, 0F);
			float pitch = tag.getFloatOr(TAG_PITCH, 0F);
			float yaw = tag.getFloatOr(TAG_YAW, 0F);
			return new Segment(pos, length, pitch, yaw);
		}
	}
}
