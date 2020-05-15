import React from 'react';
import { notification } from 'antd';
import { AppIcon } from './Icon';

export const appNotification = (message, description, iconType, iconTheme, className) =>
  notification.open({
    message,
    description,
    icon: <AppIcon className={className} type={iconType} theme={iconTheme} />,
    duration: !!description && !description.length ? 8 : 4,
  });
