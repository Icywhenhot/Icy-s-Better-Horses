package icy.betterhorses.net.client.render;

import net.minecraft.util.Mth;
import org.joml.Matrix3f;
import org.joml.Vector3f;

final class BhJumpRig {

    static final int X = 0;
    static final int Y = 1;
    static final int Z = 2;
    static final int XROT = 3;
    static final int YROT = 4;
    static final int ZROT = 5;

    private final float scale;
    private final float hipY;
    private final float hipZ;
    private final float[] rest = new float[BhJumpClips.POSE_SIZE];
    private final float[] pivotY = new float[4];
    private final float[] pivotZ = new float[4];

    final float[] out = new float[BhJumpClips.POSE_SIZE];

    private final Matrix3f bodyRot = new Matrix3f();
    private final Matrix3f legRot = new Matrix3f();
    private final Matrix3f both = new Matrix3f();
    private final Vector3f v = new Vector3f();
    private final Vector3f euler = new Vector3f();

    BhJumpRig(float scale, float bodyBottom, BhHorseModel.Rest body, BhHorseModel.Rest neck,
              BhHorseModel.Rest head, BhHorseModel.Rest leftEar, BhHorseModel.Rest rightEar,
              BhHorseModel.Rest tail, BhHorseModel.Rest[] legs, float[] levers) {
        this.scale = scale;
        this.hipY = body.y() + bodyBottom;
        this.hipZ = legs[2].z() - scale;
        put(BhJumpClips.BODY, body);
        put(BhJumpClips.NECK, neck);
        put(BhJumpClips.HEAD, head);
        put(BhJumpClips.LEFT_EAR, leftEar);
        put(BhJumpClips.RIGHT_EAR, rightEar);
        put(BhJumpClips.TAIL, tail);
        for (int i = 0; i < 4; i++) {
            put(BhJumpClips.FRONT_LEFT + i, legs[i]);
            pivotY[i] = i < 2 ? legs[i].y() - levers[i] : hipY;
            pivotZ[i] = i < 2 ? legs[i].z() : hipZ;
        }
    }

    private void put(int bone, BhHorseModel.Rest r) {
        int at = bone * 6;
        rest[at + X] = r.x();
        rest[at + Y] = r.y();
        rest[at + Z] = r.z();
        rest[at + XROT] = r.xRot();
        rest[at + YROT] = r.yRot();
        rest[at + ZROT] = r.zRot();
    }

    void solve(float[] pose, float pitchScale) {
        int b = BhJumpClips.BODY * 6;
        float bx = -pose[b] * Mth.DEG_TO_RAD * pitchScale;
        float by = -pose[b + 1] * Mth.DEG_TO_RAD;
        float bz = pose[b + 2] * Mth.DEG_TO_RAD;
        float obx = -pose[b + 3] * scale;
        float oby = -pose[b + 4] * scale;
        float obz = pose[b + 5] * scale;
        bodyRot.rotationZYX(bz, by, bx);

        bodyRot.transform(v.set(rest[b + X], rest[b + Y] - hipY, rest[b + Z] - hipZ));
        out[b + X] = v.x + obx;
        out[b + Y] = v.y + hipY + oby;
        out[b + Z] = v.z + hipZ + obz;
        out[b + XROT] = rest[b + XROT] + bx;
        out[b + YROT] = rest[b + YROT] + by;
        out[b + ZROT] = rest[b + ZROT] + bz;

        for (int bone = BhJumpClips.NECK; bone <= BhJumpClips.TAIL; bone++) {
            int at = bone * 6;
            out[at + X] = rest[at + X] - pose[at + 3] * scale;
            out[at + Y] = rest[at + Y] - pose[at + 4] * scale;
            out[at + Z] = rest[at + Z] + pose[at + 5] * scale;
            float xr = -pose[at] * Mth.DEG_TO_RAD;
            out[at + XROT] = rest[at + XROT] + (bone == BhJumpClips.NECK ? xr * pitchScale : xr);
            out[at + YROT] = rest[at + YROT] - pose[at + 1] * Mth.DEG_TO_RAD;
            out[at + ZROT] = rest[at + ZROT] + pose[at + 2] * Mth.DEG_TO_RAD;
        }

        for (int i = 0; i < 4; i++) {
            int at = (BhJumpClips.FRONT_LEFT + i) * 6;
            float lx = -pose[at] * Mth.DEG_TO_RAD;
            float ly = -pose[at + 1] * Mth.DEG_TO_RAD;
            float lz = pose[at + 2] * Mth.DEG_TO_RAD;
            float olx = -pose[at + 3] * scale;
            float oly = -pose[at + 4] * scale;
            float olz = pose[at + 5] * scale;
            float qx = rest[at + X];
            float px = qx;
            float py = pivotY[i];
            float pz = pivotZ[i];
            legRot.rotationZYX(lz, ly, lx);
            legRot.transform(v.set(qx - px, rest[at + Y] - py, rest[at + Z] - pz));
            if (i < 2) {
                v.add(px + olx, py + oly - hipY, pz + olz - hipZ);
                bodyRot.transform(v);
                out[at + X] = v.x + obx;
                out[at + Y] = v.y + hipY + oby;
                out[at + Z] = v.z + hipZ + obz;
                both.set(bodyRot).mul(legRot).getEulerAnglesZYX(euler);
                out[at + XROT] = rest[at + XROT] + euler.x;
                out[at + YROT] = rest[at + YROT] + euler.y;
                out[at + ZROT] = rest[at + ZROT] + euler.z;
            } else {
                out[at + X] = v.x + px + olx;
                out[at + Y] = v.y + py + oly;
                out[at + Z] = v.z + pz + olz;
                out[at + XROT] = rest[at + XROT] + lx;
                out[at + YROT] = rest[at + YROT] + ly;
                out[at + ZROT] = rest[at + ZROT] + lz;
            }
        }
    }
}
