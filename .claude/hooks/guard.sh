#!/bin/bash
# NorIntegrate guardrail hook
# Blocks edits to protected files that should only be changed intentionally.

INPUT=$(cat)
FILE=$(echo "$INPUT" | python3 -c "import sys, json; d=json.load(sys.stdin); print(d.get('tool_input', {}).get('file_path', '') or d.get('tool_input', {}).get('path', ''))" 2>/dev/null)

if [ -z "$FILE" ]; then
  exit 0
fi

# Protected files — require explicit user intent to modify
# (.env and .claude/settings.json are covered by the global hook: ~/.claude/hooks/protect-sensitive.sh)
PROTECTED=(
  "docs/schema.sql"
  "docs/seed.sql"
  "CLAUDE.md"
  ".claude/hooks/"
)

for PROTECTED_FILE in "${PROTECTED[@]}"; do
  if [[ "$FILE" == *"$PROTECTED_FILE"* ]]; then
    echo "Blocked: '$PROTECTED_FILE' is a protected file. Edit it manually if intentional." >&2
    exit 2
  fi
done

exit 0
