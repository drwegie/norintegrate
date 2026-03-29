"use client";

import { useState } from "react";
import type { ChecklistItemResponse } from "@/lib/api";

interface Props {
  step: ChecklistItemResponse;
  completed: boolean;
  canToggle: boolean;
  onToggle: (procedureId: number, completed: boolean) => void;
}

export function ChecklistItem({ step, completed, canToggle, onToggle }: Props) {
  const [expanded, setExpanded] = useState(false);
  const hasDetails = step.description || step.documents.length > 0;

  return (
    <li
      className={`rounded-lg border ${
        step.isNext && !completed
          ? "border-blue-300 bg-blue-50"
          : "border-gray-200 bg-white"
      }`}
    >
      <div
        className="flex items-start gap-3 p-4 cursor-pointer"
        onClick={() => hasDetails && setExpanded(!expanded)}
      >
        {canToggle ? (
          <input
            type="checkbox"
            checked={completed}
            onChange={() => onToggle(step.procedureId, completed)}
            onClick={(e) => e.stopPropagation()}
            className="mt-1 h-5 w-5 rounded border-gray-300"
          />
        ) : (
          <span className="mt-1 h-5 w-5 flex items-center justify-center text-sm text-gray-400">
            &bull;
          </span>
        )}
        <div className="flex-1">
          <h3
            className={`font-medium ${completed ? "line-through text-gray-400" : ""}`}
          >
            {step.title}
            {step.isNext && !completed && (
              <span className="ml-2 text-xs bg-blue-600 text-white px-2 py-0.5 rounded-full">
                Next step
              </span>
            )}
          </h3>
          {step.authority && (
            <p className="text-sm text-gray-500 mt-1">{step.authority}</p>
          )}
          {step.estimatedDays != null && step.estimatedDays > 0 && (
            <p className="text-xs text-gray-400 mt-1">
              Estimated: {step.estimatedDays} {step.estimatedDays === 1 ? "day" : "days"}
            </p>
          )}
        </div>
        {hasDetails && (
          <span className="mt-1 text-sm text-gray-400">
            {expanded ? "▼" : "▶"}
          </span>
        )}
      </div>
      {expanded && hasDetails && (
        <div className="px-4 pb-4 ml-8 border-t border-gray-100 pt-3">
          {step.description && (
            <p className="text-sm text-gray-600 mb-2">{step.description}</p>
          )}
          {step.documents.length > 0 && (
            <ul className="space-y-1">
              {step.documents.map((doc) => (
                <li key={doc.documentName} className="text-sm text-gray-600">
                  <span className="mr-2">•</span>
                  {doc.documentName}
                  {!doc.mandatory && (
                    <span className="ml-2 text-xs px-1.5 py-0.5 rounded bg-gray-100 text-gray-500">
                      Optional
                    </span>
                  )}
                </li>
              ))}
            </ul>
          )}
        </div>
      )}
    </li>
  );
}
