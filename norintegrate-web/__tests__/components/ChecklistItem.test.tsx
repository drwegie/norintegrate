import { screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, it, expect, vi } from "vitest";
import { ChecklistItem } from "@/components/ChecklistItem";
import type { ChecklistItemResponse } from "@/lib/api";
import { renderWithIntl } from "../test-utils";

function makeStep(
  overrides: Partial<ChecklistItemResponse> = {}
): ChecklistItemResponse {
  return {
    procedureId: 10,
    title: "Get D-nummer",
    description: null,
    authority: "Skatteetaten",
    estimatedDays: 14,
    isNext: false,
    documents: [],
    ...overrides,
  };
}

describe("ChecklistItem", () => {
  it("renders translated procedure title when translation exists", () => {
    renderWithIntl(
      <ChecklistItem
        step={makeStep({ procedureId: 1, title: "Receive job offer from Norwegian employer" })}
        completed={false}
        canToggle={false}
        onToggle={vi.fn()}
      />
    );
    expect(screen.getByText("Receive job offer from Norwegian employer")).toBeInTheDocument();
  });

  it("falls back to API title when no translation exists", () => {
    renderWithIntl(
      <ChecklistItem
        step={makeStep({ procedureId: 999, title: "Unknown procedure" })}
        completed={false}
        canToggle={false}
        onToggle={vi.fn()}
      />
    );
    expect(screen.getByText("Unknown procedure")).toBeInTheDocument();
  });

  it("renders authority", () => {
    renderWithIntl(
      <ChecklistItem
        step={makeStep()}
        completed={false}
        canToggle={false}
        onToggle={vi.fn()}
      />
    );
    expect(screen.getByText("Skatteetaten")).toBeInTheDocument();
  });

  it("shows 'Next step' badge when isNext is true and not completed", () => {
    renderWithIntl(
      <ChecklistItem
        step={makeStep({ isNext: true })}
        completed={false}
        canToggle={false}
        onToggle={vi.fn()}
      />
    );
    expect(screen.getByText("Next step")).toBeInTheDocument();
  });

  it("does NOT show 'Next step' badge when completed", () => {
    renderWithIntl(
      <ChecklistItem
        step={makeStep({ isNext: true })}
        completed={true}
        canToggle={true}
        onToggle={vi.fn()}
      />
    );
    expect(screen.queryByText("Next step")).not.toBeInTheDocument();
  });

  it("shows a checkbox when canToggle is true", () => {
    renderWithIntl(
      <ChecklistItem
        step={makeStep()}
        completed={false}
        canToggle={true}
        onToggle={vi.fn()}
      />
    );
    expect(screen.getByRole("checkbox")).toBeInTheDocument();
  });

  it("shows a bullet when canToggle is false", () => {
    renderWithIntl(
      <ChecklistItem
        step={makeStep()}
        completed={false}
        canToggle={false}
        onToggle={vi.fn()}
      />
    );
    expect(screen.queryByRole("checkbox")).not.toBeInTheDocument();
  });

  it("calls onToggle with the correct arguments when checkbox is clicked", async () => {
    const onToggle = vi.fn();
    const user = userEvent.setup();
    renderWithIntl(
      <ChecklistItem
        step={makeStep()}
        completed={false}
        canToggle={true}
        onToggle={onToggle}
      />
    );
    await user.click(screen.getByRole("checkbox"));
    expect(onToggle).toHaveBeenCalledWith(10, false);
  });

  it("shows estimated days when set", () => {
    renderWithIntl(
      <ChecklistItem
        step={makeStep({ estimatedDays: 14 })}
        completed={false}
        canToggle={false}
        onToggle={vi.fn()}
      />
    );
    expect(screen.getByText(/Estimated.*14/)).toBeInTheDocument();
  });

  it("applies line-through styling to completed items", () => {
    renderWithIntl(
      <ChecklistItem
        step={makeStep({ procedureId: 999, title: "Get D-nummer" })}
        completed={true}
        canToggle={true}
        onToggle={vi.fn()}
      />
    );
    const heading = screen.getByText("Get D-nummer");
    expect(heading).toHaveClass("line-through");
  });

  it("expands to show description on click", async () => {
    const user = userEvent.setup();
    renderWithIntl(
      <ChecklistItem
        step={makeStep({ procedureId: 999, title: "Get D-nummer", description: "Visit Skatteetaten to register." })}
        completed={false}
        canToggle={false}
        onToggle={vi.fn()}
      />
    );
    expect(screen.queryByText("Visit Skatteetaten to register.")).not.toBeInTheDocument();
    await user.click(screen.getByText("Get D-nummer"));
    expect(screen.getByText("Visit Skatteetaten to register.")).toBeInTheDocument();
  });

  it("expands to show translated documents on click", async () => {
    const user = userEvent.setup();
    renderWithIntl(
      <ChecklistItem
        step={makeStep({
          procedureId: 999,
          title: "Get D-nummer",
          documents: [
            { documentName: "Valid passport", mandatory: true },
            { documentName: "Some unknown doc", mandatory: false },
          ],
        })}
        completed={false}
        canToggle={false}
        onToggle={vi.fn()}
      />
    );
    expect(screen.queryByText("Valid passport")).not.toBeInTheDocument();
    await user.click(screen.getByText("Get D-nummer"));
    expect(screen.getByText("Valid passport")).toBeInTheDocument();
    expect(screen.getByText("Some unknown doc")).toBeInTheDocument();
    expect(screen.getByText("Optional")).toBeInTheDocument();
  });

  it("checkbox click does not toggle expand", async () => {
    const onToggle = vi.fn();
    const user = userEvent.setup();
    renderWithIntl(
      <ChecklistItem
        step={makeStep({ procedureId: 999, title: "Get D-nummer", description: "Some details here." })}
        completed={false}
        canToggle={true}
        onToggle={onToggle}
      />
    );
    await user.click(screen.getByRole("checkbox"));
    expect(onToggle).toHaveBeenCalledWith(999, false);
    expect(screen.queryByText("Some details here.")).not.toBeInTheDocument();
  });
});
