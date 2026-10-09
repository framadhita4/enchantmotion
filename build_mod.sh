#!/usr/bin/env bash
set -e

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" >/dev/null 2>&1 && pwd)"
cd "$DIR"

TARGET="${1:-all}"
MOD_VERSION="$(grep '^mod_version=' gradle.properties 2>/dev/null | cut -d= -f2 || echo '1.0.1')"
MOD_VERSION="${MOD_VERSION:-1.0.1}"
MC_VERSION="${MC_VERSION:-${MINECRAFT_VERSION:-$(grep '^minecraft_version=' gradle.properties 2>/dev/null | cut -d= -f2 || echo '26.3')}}"
MC_VERSION="${MC_VERSION:-26.3}"

echo "=========================================================="
echo "  EnchantMotion - Multi-Loader Mod Builder"
echo "  Target: $TARGET | Mod: $MOD_VERSION | Minecraft: $MC_VERSION"
echo "=========================================================="

# 1. Prepare classpath from PrismLauncher libraries prioritized for target MC version
CP_FILE="/tmp/mc_classpath_${MC_VERSION}.txt"
if [ ! -f "$CP_FILE" ] || [ ! -s "$CP_FILE" ] || ! grep -q "minecraft" "$CP_FILE"; then
    echo "==> Indexing classpath libraries from PrismLauncher for Minecraft ${MC_VERSION}..."
    {
        find ~/.local/share/PrismLauncher/libraries -name "*${MC_VERSION}*.jar"
        find ~/.local/share/PrismLauncher/libraries -name "*.jar"
    } | awk '!seen[$0]++' | tr '\n' ':' > "$CP_FILE"
fi

mkdir -p build/libs
# Clean previous builds from other Minecraft versions and old unversioned jars to focus solely on current MC_VERSION
find build/libs -maxdepth 1 -type f -name "*.jar" ! -name "*-${MC_VERSION}.jar" -delete 2>/dev/null || true

# Function to build NeoForge JAR
build_neoforge() {
    echo ""
    echo "==> [NeoForge] Building mod JAR..."
    STAGING="/tmp/ea_neoforge_staging"
    rm -rf "$STAGING" && mkdir -p "$STAGING/META-INF"

    # Compile shared core and neoforge sources
    javac --release 21 -cp "$(< "$CP_FILE")" -d "$STAGING" \
        $(find core/animation core/effects java/common java/neoforge -name "*.java")

    # Copy resources & manifests
    cp java/common/src/main/resources/enchantmotion.mixins.json "$STAGING/"
    cp java/common/src/main/resources/enchantmotion.common.json "$STAGING/"
    if [ -f java/common/src/main/resources/pack.mcmeta ]; then
        cp java/common/src/main/resources/pack.mcmeta "$STAGING/"
    fi
    if [ -f java/neoforge/src/main/resources/icon.png ]; then
        cp java/neoforge/src/main/resources/icon.png "$STAGING/"
    fi
    sed "s/\${version}/$MOD_VERSION/g" java/neoforge/src/main/resources/META-INF/neoforge.mods.toml > "$STAGING/META-INF/neoforge.mods.toml"

    # Package JAR
    OUT="build/libs/enchantmotion-neoforge-${MOD_VERSION}-${MC_VERSION}.jar"
    jar cvf "$OUT" -C "$STAGING" . > /dev/null
    echo "==> [NeoForge] Successfully built: $OUT"

    # Install to PrismLauncher instances if available
    for INST in "$HOME/.local/share/PrismLauncher/instances/${MC_VERSION}"/minecraft/mods \
                "$HOME/.local/share/PrismLauncher/instances/${MC_VERSION}_neoforge"/minecraft/mods; do
        if [ -d "$INST" ]; then
            rm -f "$INST"/enchantmotion-neoforge-*.jar
            cp -f "$OUT" "$INST"
            echo "==> [NeoForge] Installed to $INST"
        fi
    done
}

# Function to build Fabric JAR
build_fabric() {
    echo ""
    echo "==> [Fabric] Building mod JAR..."
    STAGING="/tmp/ea_fabric_staging"
    rm -rf "$STAGING" && mkdir -p "$STAGING"

    # Compile shared core and fabric sources
    javac --release 21 -cp "$(< "$CP_FILE")" -d "$STAGING" \
        $(find core/animation core/effects java/common java/fabric -name "*.java")

    # Copy resources & manifests
    cp java/common/src/main/resources/enchantmotion.mixins.json "$STAGING/"
    cp java/common/src/main/resources/enchantmotion.common.json "$STAGING/"
    if [ -f java/common/src/main/resources/pack.mcmeta ]; then
        cp java/common/src/main/resources/pack.mcmeta "$STAGING/"
    fi
    if [ -d java/fabric/src/main/resources/assets ]; then
        cp -r java/fabric/src/main/resources/assets "$STAGING/"
    fi
    sed "s/\${version}/$MOD_VERSION/g" java/fabric/src/main/resources/fabric.mod.json > "$STAGING/fabric.mod.json"

    # Package JAR
    OUT="build/libs/enchantmotion-fabric-${MOD_VERSION}-${MC_VERSION}.jar"
    jar cvf "$OUT" -C "$STAGING" . > /dev/null
    echo "==> [Fabric] Successfully built: $OUT"

    # Install to PrismLauncher instances if available
    for INST in "$HOME/.local/share/PrismLauncher/instances/${MC_VERSION}_fabric"/minecraft/mods; do
        if [ -d "$INST" ]; then
            rm -f "$INST"/enchantmotion-fabric-*.jar
            cp -f "$OUT" "$INST"
            echo "==> [Fabric] Installed to $INST"
        fi
    done
}

# Function to build Forge JAR
build_forge() {
    echo ""
    echo "==> [Forge] Building mod JAR..."
    STAGING="/tmp/ea_forge_staging"
    rm -rf "$STAGING" && mkdir -p "$STAGING/META-INF"

    # Compile shared core and forge sources
    javac --release 21 -cp "$(< "$CP_FILE")" -d "$STAGING" \
        $(find core/animation core/effects java/common java/forge -name "*.java")

    # Copy resources & manifests
    cp java/common/src/main/resources/enchantmotion.mixins.json "$STAGING/"
    cp java/common/src/main/resources/enchantmotion.common.json "$STAGING/"
    if [ -f java/common/src/main/resources/pack.mcmeta ]; then
        cp java/common/src/main/resources/pack.mcmeta "$STAGING/"
    fi
    if [ -f java/forge/src/main/resources/icon.png ]; then
        cp java/forge/src/main/resources/icon.png "$STAGING/"
    fi
    sed "s/\${version}/$MOD_VERSION/g" java/forge/src/main/resources/META-INF/mods.toml > "$STAGING/META-INF/mods.toml"
    if [ -f java/forge/src/main/resources/META-INF/MANIFEST.MF ]; then
        cp java/forge/src/main/resources/META-INF/MANIFEST.MF "$STAGING/META-INF/"
    fi

    # Package JAR with manifest
    OUT="build/libs/enchantmotion-forge-${MOD_VERSION}-${MC_VERSION}.jar"
    if [ -f "$STAGING/META-INF/MANIFEST.MF" ]; then
        jar cvfm "$OUT" "$STAGING/META-INF/MANIFEST.MF" -C "$STAGING" . > /dev/null
    else
        jar cvf "$OUT" -C "$STAGING" . > /dev/null
    fi
    echo "==> [Forge] Successfully built: $OUT"

    # Install to PrismLauncher instances if available
    for INST in "$HOME/.local/share/PrismLauncher/instances/${MC_VERSION}_forge"/minecraft/mods; do
        if [ -d "$INST" ]; then
            rm -f "$INST"/enchantmotion-forge-*.jar
            cp -f "$OUT" "$INST"
            echo "==> [Forge] Installed to $INST"
        fi
    done
}

case "$TARGET" in
    neoforge)
        build_neoforge
        ;;
    fabric)
        build_fabric
        ;;
    forge)
        build_forge
        ;;
    all|*)
        build_neoforge
        build_fabric
        build_forge
        ;;
esac

echo ""
echo "=========================================================="
echo "==> Build completed for Minecraft ${MC_VERSION}:"
ls -lh build/libs/*-${MC_VERSION}.jar 2>/dev/null || ls -lh build/libs/
echo "=========================================================="
