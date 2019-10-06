import _ from 'lodash';
import { appNotification } from '../common/notification';
import { historyBrowser } from './history';

export const errorHandler = (error, customMsg) => {
  if (!error.response) {
    return appNotification('Unexpected error', error.message);
  }
  const { status, data } = error.response;

  if (!_.isNil(status) && !_.isNil(error.message)) {
    const description = _.isString(data) ? data : data.error;
    const errorNotification = appNotification(
      error.message,
      customMsg && description ? `${customMsg} ${description}` : customMsg, //<============================================
      'frown',
    );

    switch (status) {
      case 403:
        // case 404:
        historyBrowser.push({
          pathname: '/error',
          search: `?status=${status}`,
        });
        return errorNotification;
      default:
        return errorNotification;
    }
  }

  appNotification('Something went wrong..');
};
