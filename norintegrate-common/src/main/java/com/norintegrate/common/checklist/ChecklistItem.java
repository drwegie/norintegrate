package com.norintegrate.common.checklist;

import com.norintegrate.common.procedure.Procedure;

public record ChecklistItem(Procedure procedure, boolean isNext) {}
