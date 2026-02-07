#!/bin/sh
# setup-hook.sh: Configure git to use the shared .githooks directory for hooks
# Usage: sh setup-hook.sh

set -e

# Set hooksPath to .githooks
if git config core.hooksPath .githooks; then
  echo "Git hooksPath set to .githooks."
else
  echo "Failed to set git hooksPath." >&2
  exit 1
fi

# Make sure pre-commit hook is executable
if [ -f .githooks/pre-commit ]; then
  chmod +x .githooks/pre-commit
  echo "Made .githooks/pre-commit executable."
else
  echo ".githooks/pre-commit not found. Please add the hook script." >&2
  exit 1
fi

echo "Setup complete. Shared pre-commit hook is now active for this repo."

