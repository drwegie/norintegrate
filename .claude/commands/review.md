Use the reviewer agent to review recently changed files.

First, run `git diff --name-only HEAD` to get the list of changed files. If that returns nothing, run `git status --short` to find untracked or staged files. Filter the results to only Java source files (ending in `.java`).

Then pass those files to the reviewer agent with this instruction:

"You are the reviewer agent for the NorIntegrate project. Review the following changed files: [list of files]. Check all CLAUDE.md rules: no Lombok, records for DTOs, FetchType.LAZY on associations, @Transactional correctness, constructor injection, package-by-feature, and any other rule violations. Report findings with CRITICAL / WARNING / SUGGESTION. End with overall assessment and whether it is safe to proceed."

If there are no changed Java files, report that there is nothing to review.
