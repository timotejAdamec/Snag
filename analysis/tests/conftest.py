"""Pytest conftest — puts analysis/ on sys.path so tests can import the Python
tooling modules without a package install.
"""
import sys
from pathlib import Path

ANALYSIS_DIR = Path(__file__).resolve().parent.parent
if str(ANALYSIS_DIR) not in sys.path:
    sys.path.insert(0, str(ANALYSIS_DIR))
