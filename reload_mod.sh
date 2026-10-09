#!/usr/bin/env bash
set -e

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" >/dev/null 2>&1 && pwd)"
cd "$DIR"

MC_VERSION="${MC_VERSION:-${MINECRAFT_VERSION:-$(grep '^minecraft_version=' gradle.properties 2>/dev/null | cut -d= -f2 || echo '26.3')}}"
MC_VERSION="${MC_VERSION:-26.3}"

echo "==> 1. Compiling updated sources with javac (release 21) for MC $MC_VERSION..."
# Prepare classpath from PrismLauncher libraries prioritized for target MC version
CP_FILE="/tmp/mc_classpath_${MC_VERSION}.txt"
if [ ! -f "$CP_FILE" ] || [ ! -s "$CP_FILE" ] || ! grep -q "minecraft" "$CP_FILE"; then
    echo "==> Indexing classpath libraries from PrismLauncher for Minecraft ${MC_VERSION}..."
    {
        find ~/.local/share/PrismLauncher/libraries -name "*${MC_VERSION}*.jar"
        find ~/.local/share/PrismLauncher/libraries -name "*.jar"
    } | awk '!seen[$0]++' | tr '\n' ':' > "$CP_FILE"
fi

mkdir -p /tmp/ea_jar_staging/META-INF build/libs
find build/libs -maxdepth 1 -type f -name "*.jar" ! -name "*-${MC_VERSION}.jar" -delete 2>/dev/null || true
javac --release 21 -cp "$(< "$CP_FILE")" -d /tmp/ea_jar_staging $(find core/animation core/effects java/common java/neoforge -name "*.java")

# Ensure HotSwapClient is compiled
if [ ! -f "$DIR/tools/HotSwapClient.class" ]; then
    javac "$DIR/tools/HotSwapClient.java"
fi

echo "==> 2. Hot-swapping changed classes into Minecraft..."
java -cp "$DIR" tools.HotSwapClient 5005 /tmp/ea_jar_staging || echo "==> Notice: Live HotSwap skipped (method schema/mixin changes require Minecraft restart to apply)."

echo "==> 3. Updating JAR on disk for persistence..."
cp java/common/src/main/resources/enchantmotion.mixins.json /tmp/ea_jar_staging/ 2>/dev/null || true
cp java/common/src/main/resources/enchantmotion.common.json /tmp/ea_jar_staging/ 2>/dev/null || true
cp java/common/src/main/resources/pack.mcmeta /tmp/ea_jar_staging/ 2>/dev/null || true
if [ -f java/neoforge/src/main/resources/icon.png ]; then
    cp java/neoforge/src/main/resources/icon.png /tmp/ea_jar_staging/ 2>/dev/null || true
fi
MOD_VERSION="$(grep '^mod_version=' gradle.properties 2>/dev/null | cut -d= -f2 || echo '1.0.1')"
MOD_VERSION="${MOD_VERSION:-1.0.1}"
sed "s/\${version}/$MOD_VERSION/g" java/neoforge/src/main/resources/META-INF/neoforge.mods.toml > /tmp/ea_jar_staging/META-INF/neoforge.mods.toml 2>/dev/null || true
OUT="build/libs/enchantmotion-neoforge-${MOD_VERSION}-${MC_VERSION}.jar"
jar cvf "$OUT" -C /tmp/ea_jar_staging . > /dev/null

for INST in "$HOME/.local/share/PrismLauncher/instances/${MC_VERSION}"/minecraft/mods \
            "$HOME/.local/share/PrismLauncher/instances/${MC_VERSION}_neoforge"/minecraft/mods; do
    if [ -d "$INST" ]; then
        rm -f "$INST"/enchantmotion-neoforge-*.jar
        cp -f "$OUT" "$INST"
    fi
done

echo "==> Done! Mod JAR successfully updated in PrismLauncher."
