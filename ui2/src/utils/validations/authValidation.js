import * as Yup from 'yup';


export const SignupSchema = Yup.object().shape({
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
