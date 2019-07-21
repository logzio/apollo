import axios from 'axios';

const baseUrl = "http://localhost:8081";

export const signup = async (user) => {
    try {
        const response = await axios.post(`${baseUrl}/signup/`, user);
        return response.data;

    } catch (error) {
        console.error(error);
        throw error.response.data.error;
    }
};


export const getDeploymentRoles = async () => {
    try {
        const response = await axios.get(`${baseUrl}/deployment-roles/`);
        return response.data;

    } catch (error) {
        console.error(error);
        throw error;
    }
};


export const login = async (user) => {
    try {
        const response = await axios.post(`${baseUrl}/_login/`, user);
        axios.defaults.headers.common['Authorization'] = response.data.token;
        return response.data;

    } catch (error) {
        console.error(error);
        // throw error.response.data.error;
        throw new Error('User credentials are incorrect'); //temp until an error notification will return from the server

    }
};

//TODO: error handler
// set env
// {password: "test", username:"test@gmail.com"}
