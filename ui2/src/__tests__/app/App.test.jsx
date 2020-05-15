import React from 'react';
import { mount, shallow } from 'enzyme';
import { SelectService } from '../../components/deployment/new/select/SelectService';
import { App } from '../../components/app/App';
import { Login } from '../../components/auth/Login';
import configureStore from 'redux-mock-store';
import { Provider } from 'react-redux';
import { MemoryRouter as Router } from 'react-router-dom';
import thunk from 'redux-thunk';
import { AUTH_TOKEN } from '../../api/api';

describe('App router', () => {
  let store;

  beforeEach(() => {
    const initialState = {
      auth: { user: null, isLoading: false, isAdmin: true, depRoles: null, error: null, loggedIn: true },
      deploy: { services: [] },
    };
    const middlewares = [thunk];
    const mockStore = configureStore(middlewares);
    store = mockStore(initialState);
  });

  it('valid path should redirect to Login', () => {
    localStorage.setItem(AUTH_TOKEN, 'user_is_authenticated');

    const wrapper = mount(
      <Provider store={store}>
        <Router initialEntries={['/deployment/new']}>
          <App />
        </Router>
      </Provider>,
    );

    expect(wrapper.find(SelectService)).toHaveLength(1);
    expect(wrapper.find(Login)).toHaveLength(0);
  });
});


// it('mapStateToProps should return the right value', () => {
//   // const initialState = {
//   //   auth: { user: null, isLoading: false, isAdmin: true, depRoles: null, error: null, loggedIn: false },
//   // };
//   const initialState = {
//     loggedIn: false,
//   };
//
//   const mockStore = configureStore();
//   const store = mockStore(initialState);
//
//   const wrapper = shallow(
//     <Provider store={store}>
//       <App appInit={appInit} />
//     </Provider>,
//   );
//
//   const test = wrapper.props();
//
//   expect(wrapper.props().value.store.getState().loggedIn).toBe(false);
// });

/*
const wrapper = mount(
    <Provider store={store}>
      <Router initialEntries={['/random'] appInit logout, isAdmin }}>
      <App />
    </Router>
    </Provider>,
);*/
// describe('Select services flow', () => {
//   const handleBreadcrumbs = jest.fn();
//   const getServicesStacks = jest.fn();
//   const getServices = jest.fn();
//   const selectServices = jest.fn();
//   const mockServices = [{ id: 1, name: 'sample-apollo-app', isPartOfGroup: false }];
//   const mockServicesStacks = [
//     {
//       id: 4,
//       name: 'Web App',
//       stackType: 'SERVICES',
//       services: {
//         0: 3,
//         1: 60,
//         2: 69,
//         3: 84,
//       },
//       enabled: true,
//     },
//   ];
//   const match = { path: '/deployment/new/service', url: '/deployment/new/service', isExact: true, params: {} };
//
//   const initialState = { services: [] };
//   const mockStore = configureStore();
//   let store = mockStore(initialState);
//   //
//   // beforeEach(() => {
//   //   store = mockStore(initialState);
//   //   container = mount(<ConnectedHome store={store} />);
//   // });
//
//   it('should select and transfer a service', () => {
//     const selectServiceComp = mount(
//       <Provider store={store}>
//         <Router>
//           <SelectService
//             getServices={getServices}
//             services={mockServices}
//             handleBreadcrumbs={handleBreadcrumbs}
//             getServicesStacks={getServicesStacks}
//             selectServices={selectServices}
//             mockservicesStacks={mockServicesStacks}
//             match={match}
//           />
//         </Router>
//       </Provider>,
//     );
//
//     const tableRecord = selectServiceComp.find('.ant-checkbox-input').first();
//     const transferBtn = selectServiceComp.find('.ant-btn-icon-only').first();
//     const test = transferBtn.props();
//     const tableRecordtest = tableRecord.props();
//     // expect(transferBtn.length).toEqual(1);
//     expect(transferBtn.props()['disabled']).toBe(true);
//     tableRecord.simulate('change', { target: { checked: true } });
//     const test1 = transferBtn.props();
//     // const tableRecordtest1 = tableRecord.props()['onChange'].toBeCalled();
//     // expect(transferBtn.props()['disabled']).toBe(true);
//     // const tablePlaceholder = selectServiceComp.find('.ant-table-placeholder');
//     // expect(tablePlaceholder.length).toEqual(1);
//   });
//
//   it('should change initial state', () => {
//     const selectServiceComp = mount(
//       <Provider store={store}>
//         <Router>
//           <SelectService
//             getServices={getServices}
//             services={mockServices}
//             handleBreadcrumbs={handleBreadcrumbs}
//             getServicesStacks={getServicesStacks}
//             selectServices={selectServices}
//             mockservicesStacks={mockServicesStacks}
//             match={match}
//           />
//         </Router>
//       </Provider>,
//     );
//
//     const action = store.getActions();
//     const test = selectServiceComp.find('.ant-checkbox-input').first();
//   });
// });
