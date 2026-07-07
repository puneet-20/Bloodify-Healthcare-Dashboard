import numpy as np
import tensorflow as tf
from tensorflow import keras
from tensorflow.keras import layers
from sklearn.model_selection import train_test_split

IMG_SIZE = 96
NUM_CLASSES = 8
LABELS = ["A+", "A-", "AB+", "AB-", "B+", "B-", "O+", "O-"]

def load_online_data():
    data = np.load("online_data.npz")
    images = data["images"]
    labels = data["labels"]
    # add channel dimension: (N, 96, 96) -> (N, 96, 96, 1)
    images = images.reshape(-1, IMG_SIZE, IMG_SIZE, 1)
    return images, labels

def load_manual_data():
    data = np.load("manual_data.npz")
    images = data["images"]
    labels = data["labels"]
    images = images.reshape(-1, IMG_SIZE, IMG_SIZE, 1)
    return images, labels

def build_model():
    model = keras.Sequential([
        layers.Input(shape=(IMG_SIZE, IMG_SIZE, 1)),

        layers.Conv2D(32, (3, 3), activation="relu", padding="same"),
        layers.BatchNormalization(),
        layers.MaxPooling2D((2, 2)),

        layers.Conv2D(64, (3, 3), activation="relu", padding="same"),
        layers.BatchNormalization(),
        layers.MaxPooling2D((2, 2)),

        layers.Conv2D(128, (3, 3), activation="relu", padding="same"),
        layers.BatchNormalization(),
        layers.MaxPooling2D((2, 2)),

        layers.Flatten(),
        layers.Dense(128, activation="relu"),
        layers.Dropout(0.4),
        layers.Dense(NUM_CLASSES, activation="softmax"),
    ])

    model.compile(
        optimizer="adam",
        loss="sparse_categorical_crossentropy",
        metrics=["accuracy"],
    )
    return model

def main():
    print("Loading online dataset...")
    X, y = load_online_data()
    print(f"  Loaded {X.shape[0]} images, shape {X.shape}")

    # Split: 70% train, 15% val, 15% test -- stratified so each split has all classes proportionally
    X_train, X_temp, y_train, y_temp = train_test_split(
        X, y, test_size=0.30, random_state=42, stratify=y
    )
    X_val, X_test, y_val, y_test = train_test_split(
        X_temp, y_temp, test_size=0.50, random_state=42, stratify=y_temp
    )

    print(f"  Train: {X_train.shape[0]}  Val: {X_val.shape[0]}  Test: {X_test.shape[0]}")

    print()
    print("Building model...")
    model = build_model()
    model.summary()

    print()
    print("Training...")
    early_stop = keras.callbacks.EarlyStopping(
        monitor="val_accuracy", patience=8, restore_best_weights=True
    )

    history = model.fit(
        X_train, y_train,
        validation_data=(X_val, y_val),
        epochs=40,
        batch_size=32,
        callbacks=[early_stop],
        verbose=2,
    )

    print()
    print("=" * 50)
    print("EVALUATION ON ONLINE TEST SET")
    print("=" * 50)
    test_loss, test_acc = model.evaluate(X_test, y_test, verbose=0)
    print(f"  Test accuracy: {test_acc:.4f}")
    print(f"  Test loss: {test_loss:.4f}")

    print()
    print("=" * 50)
    print("EVALUATION ON MANUAL (REAL-WORLD) DATASET")
    print("=" * 50)
    X_manual, y_manual = load_manual_data()
    manual_loss, manual_acc = model.evaluate(X_manual, y_manual, verbose=0)
    print(f"  Manual dataset accuracy: {manual_acc:.4f}")
    print(f"  Manual dataset loss: {manual_loss:.4f}")

    print()
    print("Saving model to fingerprint_model.keras ...")
    model.save("fingerprint_model.keras")
    print("Done. Model saved.")

if __name__ == "__main__":
    main()