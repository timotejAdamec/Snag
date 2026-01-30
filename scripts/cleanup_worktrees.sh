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

git worktree list | grep -v "\[main\]" | while read worktree branch commit; do
  branch_name=$(echo $branch | sed 's/\[//g' | sed 's/\]//g')

  if git branch --merged main | grep -q "$branch_name"; then
    echo "Removing merged worktree: $worktree ($branch_name)"
    git worktree remove "$worktree"
    git branch -d "$branch_name"
  else
    echo "Keeping unmerged worktree: $worktree ($branch_name)"
  fi
done

echo "Cleanup complete!"
