import type { HTMLAttributes, Ref } from "react";
import { forwardRef } from "react";
import { PopoverRoot } from "./ColorSelectorPopover.styled";
import { ColorSelectorPanel } from "metabase/common/components/ColorSelectorPanel/ColorSelectorPanel";

export interface ColorSelectorPopoverProps
  extends Omit<HTMLAttributes<HTMLDivElement>, "onChange"> {
  value?: string;
  colors: string[];
  onChange?: (newValue: string) => void;
  onClose?: () => void;
}

const ColorSelectorPopover = forwardRef(function ColorSelector(
  { value, colors, onChange, onClose, ...props }: ColorSelectorPopoverProps,
  ref: Ref<HTMLDivElement>,
) {

  return (
    <PopoverRoot {...props} ref={ref}>
      <ColorSelectorPanel
        initalColor={value}
        onChange={onChange}
        onClose={onClose}
      />
    </PopoverRoot>
  );
});

// eslint-disable-next-line import/no-default-export -- deprecated usage
export default ColorSelectorPopover;
