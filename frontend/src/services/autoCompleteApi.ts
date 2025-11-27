import axios from "axios";
import type { AutoCompleteParams, AutoCompleteResponse } from "../constants";

const BASE_URL="http://localhost:8000/api"

const api=axios.create({
    baseURL:BASE_URL,
    timeout:5000,
    headers:{
        "Content-Type":"application/json"
    }
})


export const getSuggestion=async(params:AutoCompleteParams):Promise<AutoCompleteResponse>=>{
    const response=await api.get<AutoCompleteResponse>("/autocomplete",{params});
    return response.data;
}