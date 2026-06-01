#!/usr/bin/env python3
"""Train a tiny MNIST model and export android/app/src/main/assets/digit_mnist.tflite.

This script is intentionally separate from the Android build because TensorFlow
is heavy and should not be required for every Gradle invocation.
"""

from __future__ import annotations

from pathlib import Path
import sys


ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "android" / "app" / "src" / "main" / "assets" / "digit_mnist.tflite"


def main() -> int:
    try:
        import tensorflow as tf
    except Exception as exc:  # pragma: no cover - explicit operator guidance
        print("TensorFlow is required to train the digit model.", file=sys.stderr)
        print("Install it in a local venv, then rerun:", file=sys.stderr)
        print("  python3 -m venv tools/.venv", file=sys.stderr)
        print("  source tools/.venv/bin/activate", file=sys.stderr)
        print("  pip install tensorflow", file=sys.stderr)
        print(f"Original import error: {exc}", file=sys.stderr)
        return 2

    (x_train, y_train), (x_test, y_test) = tf.keras.datasets.mnist.load_data()
    x_train = x_train.astype("float32") / 255.0
    x_test = x_test.astype("float32") / 255.0
    x_train = x_train[..., None]
    x_test = x_test[..., None]

    model = tf.keras.Sequential(
        [
            tf.keras.layers.Input(shape=(28, 28, 1)),
            tf.keras.layers.Conv2D(16, 3, activation="relu"),
            tf.keras.layers.MaxPooling2D(),
            tf.keras.layers.Conv2D(32, 3, activation="relu"),
            tf.keras.layers.Flatten(),
            tf.keras.layers.Dense(64, activation="relu"),
            tf.keras.layers.Dense(10, activation="softmax"),
        ]
    )
    model.compile(
        optimizer="adam",
        loss="sparse_categorical_crossentropy",
        metrics=["accuracy"],
    )
    model.fit(x_train, y_train, validation_data=(x_test, y_test), epochs=2, batch_size=128)

    converter = tf.lite.TFLiteConverter.from_keras_model(model)
    converter.optimizations = [tf.lite.Optimize.DEFAULT]
    tflite_model = converter.convert()

    OUT.parent.mkdir(parents=True, exist_ok=True)
    OUT.write_bytes(tflite_model)
    print(f"Wrote {OUT} ({len(tflite_model):,} bytes)")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
