import axios from "axios";
import type { FilterParams, SearchParams, SearchResponse } from "../constants";


const BASE_URL="http://localhost:8000/api";

const api=axios.create({
    baseURL: BASE_URL,
    timeout: 1000,
    headers:{
        'Content-Type':'Application/json'
    }
})

export const search=async(params:SearchParams):Promise<SearchResponse>=>{
    const response=await api.get<SearchResponse>('/search',{params});
    return response.data;
}

export const filteredSearch=async(params:FilterParams):Promise<SearchResponse>=>{
    const response=await api.get<SearchResponse>('/search/filter',{params})
    return response.data;
}

export const advanceSearch=async(params:FilterParams):Promise<SearchResponse>=>{
    const response=await api.get<SearchResponse>('/search/advanced',{params})
    return response.data;
}

export const fuzzySearch=async(params:FilterParams):Promise<SearchResponse>=>{
    const response=await api.get<SearchResponse>('/search/fuzzy',{params})
    return response.data;
}

export const rankingSearch=async(params:FilterParams):Promise<SearchResponse>=>{
    const response=await api.get<SearchResponse>('/search/ranking',{params})
    return response.data;
}

