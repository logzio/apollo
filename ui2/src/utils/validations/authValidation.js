import * as Yup from 'yup';

//TODO: change according to server validations
export const signupSchema = Yup.object().shape({
    firstName: Yup.string()
        .min(2, 'Too Short')
        .max(50, 'Too Long')
        .required('Required'),
    lastName: Yup.string()
        .min(2, 'Too Short')
        .max(50, 'Too Long')
        .required('Required'),
    userEmail: Yup.string()
        .email('Invalid email')
        .required('Required'),
    password: Yup.string()
        .min(3, 'Too Short')
        .required('Required'),
});


export const loginSchema = Yup.object().shape({
    username: Yup.string()
        .email('Invalid email')
        .required('Required'),
    password: Yup.string()
        .min(3, 'Too Short')
        .required('Required'),
});
