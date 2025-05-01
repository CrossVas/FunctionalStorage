package com.buuz135.functionalstorage.util;

import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;

public class MathUtils {
    public static Matrix4f createTransformMatrix(Vector3f translation, Vector3f eulerDegrees, Vector3f scale) {
        Quaternion q = fromXYZDegrees(eulerDegrees);
        return createTransformMatrix(translation, q, scale);
    }

    public static Matrix4f createTransformMatrix(Vector3f translation, Vector3f eulerDegrees, float scale) {
        return createTransformMatrix(translation, eulerDegrees, new Vector3f(scale, scale, scale));
    }

    public static Matrix4f createTransformMatrix(Vector3f translation, Quaternion rotation, Vector3f scale) {
        Matrix4f transform = Matrix4f.createTranslateMatrix(translation.x(), translation.y(), translation.z());
        transform.multiply(rotation);
        Matrix4f scaleMat = Matrix4f.createScaleMatrix(scale.x(), scale.y(), scale.z());
        transform.multiply(scaleMat);
        return transform;
    }

    public static Matrix4f createTransformMatrix(Vector3f translation, Quaternion rotation, float scale) {
        return createTransformMatrix(translation, rotation, new Vector3f(scale, scale, scale));
    }

    public static Quaternion fromXYZDegrees(Vector3f pDegreesVector) {
        return fromXYZ((float) Math.toRadians(pDegreesVector.x()), (float) Math.toRadians(pDegreesVector.y()), (float) Math.toRadians(pDegreesVector.z()));
    }

    public static Quaternion fromXYZ(float pX, float pY, float pZ) {
        Quaternion quaternion = Quaternion.ONE.copy();
        quaternion.mul(new Quaternion((float) Math.sin(pX / 2.0F), 0.0F, 0.0F, (float) Math.cos(pX / 2.0F)));
        quaternion.mul(new Quaternion(0.0F, (float) Math.sin(pY / 2.0F), 0.0F, (float) Math.cos(pY / 2.0F)));
        quaternion.mul(new Quaternion(0.0F, 0.0F, (float) Math.sin(pZ / 2.0F), (float) Math.cos(pZ / 2.0F)));
        return quaternion;
    }
}
