// import React from 'react';
// import {mount} from 'enzyme';
// import SignupForm from '../../components/auth/SignupForm';
//
// describe('Add new user', () => {
//     const onSubmitFn = jest.fn();
//
//
//     it('should subscribe a new user', () => {
//         const signupFormComp = mount(<SignupForm handleSubmit={onSubmitFn} options={[{id: 1, name: 1}]}/>);
//         const form = signupFormComp.find('form');
//         expect(form.length).toEqual(1);
//         form.simulate("change", {firstName: 'ifat', lastName: 'regev', userEmail: 'asIf@gmail.com', password: 'asIf'}, true);
//         const firstName = signupFormComp.find(".input-firstName");
//         expect(firstName.length).toEqual(1);
//         const submitBtn = signupFormComp.find("button");
//         expect(submitBtn.length).toEqual(1);
//         expect(submitBtn.prop('type')).toEqual('submit');
//         submitBtn.simulate("click");
//         expect(onSubmitFn).toHaveBeenCalled();
//     });
// });
