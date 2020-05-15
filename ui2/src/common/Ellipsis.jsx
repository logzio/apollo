import React from 'react';
import { Tooltip, Typography } from 'antd';
import './Ellipsis.css';

export const AppEllipsis = ({ children, tooltipText, isEllipsis, ...props }) => {
  return isEllipsis ? (
    <Tooltip title={tooltipText} {...props}>
      <Typography.Paragraph ellipsis={isEllipsis} className="ellipsis-text">
        {children}
      </Typography.Paragraph>
    </Tooltip>
  ) : (
    children
  );
};
