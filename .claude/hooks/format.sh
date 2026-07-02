#!/bin/bash
# PostToolUse hook: auto-format Java files with Spotless after agent edits.
# Non-Java files and failures are ignored silently (formatting is best-effort).

INPUT=$(cat)
FILE=$(echo "$INPUT" | python3 -c "import sys, json; d=json.load(sys.stdin); print(d.get('tool_input', {}).get('file_path', ''))" 2>/dev/null)

[[ "$FILE" != *.java ]] && exit 0
cd "$CLAUDE_PROJECT_DIR" 2>/dev/null || exit 0
[ -x ./gradlew ] || exit 0

case "$FILE" in
  *norintegrate-common/*) MODULE=":norintegrate-common" ;;
  *norintegrate-api/*)    MODULE=":norintegrate-api" ;;
  *norintegrate-mcp/*)    MODULE=":norintegrate-mcp" ;;
  *) exit 0 ;;
esac

./gradlew -q "$MODULE:spotlessApply" >/dev/null 2>&1 || true
exit 0
