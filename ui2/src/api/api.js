import axios from 'axios';

const baseUrl = "http://localhost:8081";

export const signup = async(user) => {
    try {
        const tempHeader = {Authorization: "E5FcDwV65B_Arplwfdkg1kHq440rJlDfqGaimrUTeSaOfueIVmKtFYNNMV9vvwDxuOZgyjXF714q1dhgJC0s7-fEGmIHDs_J7DUWigiwEYZ1_hmk4nTxBfq15_1Zj4uCQYiNCbhXpbevOMRf5co1eMOD2_k="};
        const response = await axios.post(`${baseUrl}/signup/`, user, {headers: tempHeader});
        return response.data;

    } catch (error) {
        console.error(error);
    }
};


// axios.defaults.headers.common['Authorization'] = AUTH_TOKEN;
//set errorhandler
//set env
