import { useCallback } from "react";
import { ColorSelectorPanel } from "../ColorSelectorPanel";
import type { HTMLAttributes } from "react";
import {colors} from "metabase/lib/colors/palette";
import chroma from "chroma-js";

export interface ColorRangeSelectorPanelProps
  extends Omit<HTMLAttributes<HTMLDivElement>, "onChange"> {
  initialValue?: string[];
  onChange?: (newValue: string[]) => void;
  onClose?: () => void;
}

function generateColorShades(hex: string, steps: number = 5): string[] {
  return chroma
    .scale([chroma(hex).darken(2), hex, chroma(hex).brighten(2)])
    .mode("lab")
    .colors(steps);
}

export const ColorRangeSelectorPanel = ({
  initialValue,
  onChange,
  onClose,
//   invertRange = false
}: ColorRangeSelectorPanelProps) => {
  const handleColorChange = useCallback(
    (newValue: string) => {
      const shades = generateColorShades(newValue);
      onChange?.(shades);
    },
    [onChange],
  );

  return (
    <ColorSelectorPanel
        initalColor={initialValue?.[2] ?? colors.white}
      onChange={handleColorChange}
      onClose={onClose}
      style={{ border: "none" }}
    />
  );
};
