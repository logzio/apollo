import React from 'react';
import { errorHandler } from '../../utils/errorHandler';
import { appNotification } from '../../common/notification';
import { Container } from '../../components/app/Container';
import { mount, shallow } from 'enzyme';

// describe('Error Handling Service', () => {
// it('should handle breadcrumb addition', async () => {
//   const component = mount(<Container />);
//   // const submitButton = component.find(Button);
//   // submitButton.simulate('click');
//   // expect(submitButton.prop('disabled')).toBeTruthy();
//   const spy = jest.spyOn(component.instance(), 'handleBreadcrumbs');
// });

//
// it('should handle API unexpected error', async () => {
//     const customMsg = 'Unexpected title';
//     const error = { message: 'test', response: { status: 500, data: 'hi' } };
//
//     jest.spyOn(appNotification, 'danger').mockImplementation(jest.fn());
//
//     await errorHandler({ error, customMsg });
//
//     expect(appNotification).toHaveBeenCalledWith(error.message, customMsg, 'frown');
// });

// it('should handle UI unexpected error', async () => {
//     const title = 'Unexpected title';
//     const subject = 'Unexpected subject';
//     const unexpectedErrorLogMessage = 'Unexpected log message';
//     const error = new Error();
//     const logger = getLogger();
//
//     jest.spyOn(ngInjector, 'getAngularService').mockResolvedValue(logger);
//     jest.spyOn(NotificationService, 'unexpectedError').mockImplementation(jest.fn());
//
//     await ErrorHandlingService.handleUiError({ title, unexpectedErrorLogMessage, error, subject });
//
//     expect(NotificationService.unexpectedError).toHaveBeenCalledWith(title, subject);
// });
// });
