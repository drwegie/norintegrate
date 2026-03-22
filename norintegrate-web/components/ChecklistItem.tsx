import type { ChecklistStep } from "@/lib/api";

interface Props {
  step: ChecklistStep;
  completed: boolean;
  canToggle: boolean;
  onToggle: (procedureId: number, completed: boolean) => void;
}

export function ChecklistItem({ step, completed, canToggle, onToggle }: Props) {
  const { procedure } = step;

  return (
    <li
      className={`flex items-start gap-3 p-4 rounded-lg border ${
        step.isNextStep && !completed
          ? "border-blue-300 bg-blue-50"
          : "border-gray-200 bg-white"
      }`}
    >
      {canToggle ? (
        <input
          type="checkbox"
          checked={completed}
          onChange={() => onToggle(procedure.id, completed)}
          className="mt-1 h-5 w-5 rounded border-gray-300"
        />
      ) : (
        <span className="mt-1 h-5 w-5 flex items-center justify-center text-sm text-gray-400">
          {step.displayOrder}
        </span>
      )}
      <div className="flex-1">
        <h3
          className={`font-medium ${completed ? "line-through text-gray-400" : ""}`}
        >
          {procedure.name}
          {step.isNextStep && !completed && (
            <span className="ml-2 text-xs bg-blue-600 text-white px-2 py-0.5 rounded-full">
              Next step
            </span>
          )}
        </h3>
        <p className="text-sm text-gray-600 mt-1">{procedure.description}</p>
        {procedure.officialUrl && (
          <a
            href={procedure.officialUrl}
            target="_blank"
            rel="noopener noreferrer"
            className="text-sm text-blue-600 hover:underline mt-1 inline-block"
          >
            Official information
          </a>
        )}
      </div>
    </li>
  );
}
