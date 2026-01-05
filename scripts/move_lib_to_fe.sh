#!/bin/bash

#
# Copyright (c) 2026 Timotej Adamec
# SPDX-License-Identifier: MIT
#
# This file is part of the thesis:
# "Multiplatform snagging system with code sharing maximisation"
#
# Czech Technical University in Prague
# Faculty of Information Technology
# Department of Software Engineering
#

LIB_DIR="lib"
BASE_PACKAGE="cz/adamec/timotej/snag/lib"
BASE_PACKAGE_DOT="cz.adamec.timotej.snag.lib"

for module_dir in "$LIB_DIR"/*; do
    if [ -d "$module_dir/fe" ]; then
        module_name=$(basename "$module_dir")
        echo "Processing module: $module_name"

        # 1. Move files in each source set
        find "$module_dir/fe/src" -name "kotlin" -type d | while read -r kotlin_dir; do
            target_pkg_dir="$kotlin_dir/$BASE_PACKAGE/$module_name"
            if [ -d "$target_pkg_dir" ]; then
                new_pkg_dir="$target_pkg_dir/fe"
                mkdir -p "$new_pkg_dir"
                
                # Move everything except the 'fe' directory itself
                find "$target_pkg_dir" -maxdepth 1 ! -path "$target_pkg_dir" ! -name "fe" -exec mv {} "$new_pkg_dir/" \;
            fi
        done

        # 2. Update package declarations and imports
        old_pkg="$BASE_PACKAGE_DOT.$module_name"
        new_pkg="$BASE_PACKAGE_DOT.$module_name.fe"
        
        echo "Updating package names from $old_pkg to $new_pkg"

        # Update package declarations in the module itself
        find "$module_dir/fe" -name "*.kt" -exec sed -i '' "s/package $old_pkg/package $new_pkg/g" {} +

        # Update imports in the entire project
        find . -name "*.kt" -exec sed -i '' "s/import $old_pkg/import $new_pkg/g" {} +
        find . -name "*.kts" -exec sed -i '' "s/import $old_pkg/import $new_pkg/g" {} +
    fi
done

echo "Finished moving lib modules to fe subdirectory structure."
