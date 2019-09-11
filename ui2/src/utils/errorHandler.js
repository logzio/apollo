import _ from 'lodash';
import { appNotification } from '../common/notification';
import { historyBrowser } from './history';

export const errorHandler = ({
  error: {
    response: { status, data },
    message,
  },
}) => {
  if (!_.isNil(status) && !_.isNil(message)) {
    const description = _.isString(data) ? data : data.error;

    switch (status) {
      case 403:
      case 404:
        historyBrowser.push({
          pathname: '/error',
          search: `?status=${status}`,
        });
        return appNotification(message, description, 'frown');
      default:
        return appNotification(message, description, 'frown');
    }
  }

  appNotification('Something went wrong..');
};
