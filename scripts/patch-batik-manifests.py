#!/usr/bin/env python3
"""
patch-batik-manifests.py
~~~~~~~~~~~~~~~~~~~~~~~~
Fixes the broken MANIFEST.MF Class-Path entries in the Batik 1.6
batik-rasterizer and batik-squiggle JARs that are cached in the
local Maven repository (~/.m2).

Background
----------
The Batik 1.6 JARs published to Maven Central carry Windows-style
backslash line continuations (``\\``) in their MANIFEST.MF Class-Path
entries.  Java 9+ rejects these as illegal characters when building
file URIs, causing javac to fail with:

    error reading .../batik-rasterizer-1.6.jar;
    java.net.URISyntaxException: Illegal character in path ...

The fix removes the Class-Path entry entirely (it is not needed for
compilation – all transitive dependencies are declared explicitly in
pom.xml).

Usage
-----
    python3 scripts/patch-batik-manifests.py

Called automatically by the CI workflow after ``mvn dependency:resolve``
has populated the local Maven repository.
"""

import os
import shutil
import tempfile
import zipfile

M2_REPO = os.path.join(os.path.expanduser("~"), ".m2", "repository")

JARS_TO_PATCH = [
    os.path.join(M2_REPO, "batik", "batik-rasterizer", "1.6", "batik-rasterizer-1.6.jar"),
    os.path.join(M2_REPO, "batik", "batik-squiggle",   "1.6", "batik-squiggle-1.6.jar"),
]


def _unfold_manifest(raw: str) -> list[str]:
    """Unfold MANIFEST continuation lines (lines starting with a space)."""
    lines = raw.replace("\r\n", "\n").replace("\r", "\n").split("\n")
    full_lines: list[str] = []
    current = ""
    for line in lines:
        if line.startswith(" ") and current:
            current += line[1:]
        else:
            if current:
                full_lines.append(current)
            current = line
    if current:
        full_lines.append(current)
    return full_lines


def _fold_manifest(lines: list[str]) -> str:
    """Re-fold lines to the 72-byte MANIFEST spec limit."""
    folded: list[str] = []
    for line in lines:
        encoded = line.encode("utf-8")
        if len(encoded) <= 72:
            folded.append(line)
        else:
            folded.append(encoded[:72].decode("utf-8"))
            remaining = encoded[72:]
            while remaining:
                chunk = b" " + remaining[:71]
                folded.append(chunk.decode("utf-8"))
                remaining = remaining[71:]
    return "\r\n".join(folded) + "\r\n"


def patch_jar(jar_path: str) -> None:
    if not os.path.exists(jar_path):
        print(f"  SKIP  (not found): {jar_path}")
        return

    tmp_dir = tempfile.mkdtemp(prefix="batik-patch-")
    try:
        # Extract
        with zipfile.ZipFile(jar_path, "r") as zf:
            zf.extractall(tmp_dir)

        # Read and fix manifest
        manifest_path = os.path.join(tmp_dir, "META-INF", "MANIFEST.MF")
        with open(manifest_path, "rb") as f:
            raw = f.read().decode("utf-8", errors="replace")

        full_lines = _unfold_manifest(raw)
        # Drop the Class-Path entry – the backslash continuations in it
        # are what triggers the URISyntaxException.
        fixed_lines = [l for l in full_lines if not l.startswith("Class-Path:")]
        new_manifest = _fold_manifest(fixed_lines)

        with open(manifest_path, "w", newline="") as f:
            f.write(new_manifest)

        # Repack (MANIFEST.MF must appear first in the ZIP)
        tmp_jar = jar_path + ".patching"
        with zipfile.ZipFile(tmp_jar, "w", zipfile.ZIP_DEFLATED) as zf:
            zf.write(manifest_path, "META-INF/MANIFEST.MF")
            for root, _dirs, files in os.walk(tmp_dir):
                for fname in files:
                    fpath = os.path.join(root, fname)
                    arcname = os.path.relpath(fpath, tmp_dir)
                    if arcname == os.path.join("META-INF", "MANIFEST.MF"):
                        continue
                    zf.write(fpath, arcname)

        os.replace(tmp_jar, jar_path)

        # Invalidate stale checksums so Maven does not flag the patched JAR
        for ext in (".sha1", ".md5", ".sha256"):
            chk = jar_path + ext
            if os.path.exists(chk):
                os.remove(chk)

        print(f"  FIXED: {jar_path}")
    finally:
        shutil.rmtree(tmp_dir, ignore_errors=True)


def main() -> None:
    print("Patching Batik 1.6 MANIFEST.MF files in local Maven repository …")
    for jar in JARS_TO_PATCH:
        patch_jar(jar)
    print("Done.")


if __name__ == "__main__":
    main()
