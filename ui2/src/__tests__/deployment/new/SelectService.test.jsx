import React from 'react';
import { mount } from 'enzyme';
import configureStore from 'redux-mock-store';
import { Provider } from 'react-redux';
import { MemoryRouter as Router } from 'react-router-dom';
import { SelectService } from '../../../components/deployment/new/SelectService';

describe('Select services flow', () => {
  const handleBreadcrumbs = jest.fn();
  const getServicesStacks = jest.fn();
  const getServices = jest.fn();
  const selectServices = jest.fn();
  const mockServices = [{ id: 1, name: 'sample-apollo-app', isPartOfGroup: false }];
  const mockServicesStacks = [
    {
      id: 4,
      name: 'Web App',
      stackType: 'SERVICES',
      services: {
        0: 3,
        1: 60,
        2: 69,
        3: 84,
      },
      enabled: true,
    },
  ];
  const match = { path: '/deployment/new/service', url: '/deployment/new/service', isExact: true, params: {} };

  const initialState = { services: [] };
  const mockStore = configureStore();
  let store = mockStore(initialState);
  //
  // beforeEach(() => {
  //   store = mockStore(initialState);
  //   container = mount(<ConnectedHome store={store} />);
  // });

  it('should select and transfer a service', () => {
    const selectServiceComp = mount(
      <Provider store={store}>
        <Router>
          <SelectService
            getServices={getServices}
            services={mockServices}
            handleBreadcrumbs={handleBreadcrumbs}
            getServicesStacks={getServicesStacks}
            selectServices={selectServices}
            mockservicesStacks={mockServicesStacks}
            match={match}
          />
        </Router>
      </Provider>,
    );

    const tableRecord = selectServiceComp.find('.ant-checkbox-input').first();
    const transferBtn = selectServiceComp.find('.ant-btn-icon-only').first();
    const test = transferBtn.props();
    const tableRecordtest = tableRecord.props();
    // expect(transferBtn.length).toEqual(1);
    expect(transferBtn.props()['disabled']).toBe(true);
    tableRecord.simulate('change', { target: { checked: true } });
    const test1 = transferBtn.props();
    // const tableRecordtest1 = tableRecord.props()['onChange'].toBeCalled();
    // expect(transferBtn.props()['disabled']).toBe(true);
    // const tablePlaceholder = selectServiceComp.find('.ant-table-placeholder');
    // expect(tablePlaceholder.length).toEqual(1);
  });

  it('should change initial state', () => {
    const selectServiceComp = mount(
      <Provider store={store}>
        <Router>
          <SelectService
            getServices={getServices}
            services={mockServices}
            handleBreadcrumbs={handleBreadcrumbs}
            getServicesStacks={getServicesStacks}
            selectServices={selectServices}
            mockservicesStacks={mockServicesStacks}
            match={match}
          />
        </Router>
      </Provider>,
    );

    const action = store.getActions();
    const test = selectServiceComp.find('.ant-checkbox-input').first();
  });
});
