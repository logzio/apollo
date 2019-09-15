import React from 'react';
import { notification } from 'antd';
import { AppIcon } from './Icon';

export const appNotification = (message, iconType, iconTheme, className) =>
  notification.open({
    message: message,
    icon: <AppIcon className={className} type={iconType} theme={iconTheme} />,
  });
