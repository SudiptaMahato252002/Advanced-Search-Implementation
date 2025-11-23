import { useCallback, useEffect, useRef, useState } from "react"
import type { SearchParams, SearchResponse } from "../constants"
import { useDebounce } from "./useDebounce"
import { searchProduct } from "../services/searchApi"

interface UseSearchOptions
{
    debounceMs?:number,
    minQueryLength?:number,
    initialPage?:number,
    pageSize?:number
}

interface UseSearchReturn
{
    query:string,
    setQuery:(q:string)=>void
    results:SearchResponse|null
    isLoading:boolean,
    error:string|null,
    page:number,
    setPage:(p:number)=>void
    search:(q?:string)=>Promise<void>,
    clearResults:()=>void

}

export function useSearch(options:UseSearchOptions={}):UseSearchReturn
{
    const {debounceMs=300,minQueryLength=2,initialPage=0,pageSize=10}=options

    const [query, setQuery] = useState('')
    const [results,setResults]=useState<SearchResponse|null>(null)
    const [isLoading,setIsLoading]=useState(false)
    const [error,setError]=useState<string|null>(null)
    const [page,setPage]=useState(initialPage)


    const debounceQuery=useDebounce(query,debounceMs)
    const AbortControllerRef=useRef<AbortController|null>(null)

    const search=useCallback(async(searchQuery?:string)=>{
        
        const q=searchQuery??debounceQuery
        console.log("🔎 Search Triggered with:", q);
        if(q.trim().length<minQueryLength)
        {
             console.log("🚫 Query too short, skipping search");
            setResults(null)
            return;
        }
        if(AbortControllerRef.current)
        {
            console.log("🛑 Aborting previous request");
            AbortControllerRef.current.abort()
        }
         AbortControllerRef.current=new AbortController()
        setIsLoading(true)
        setError(null)

        try 
        {
            console.log("📡 Calling API with:", q);
            const params:SearchParams={q:q.trim(),page,size:pageSize}
            const response=await searchProduct(params)
            console.log("📥 API Response Received:", response);
            setResults(response)


            
        } 
        catch (error:any) 
        {
            if(error.name !== 'AbortError' && error.name !== 'CanceledError')
            {
                setError(error.message||'Search failed. Please try again.')
                setResults(null)
            }   
        }
        finally{
            setIsLoading(false)
        }
    },[debounceQuery,page,pageSize,minQueryLength])

    useEffect(()=>{
        if(debounceQuery.trim().length>=minQueryLength)
        {
            search();
        }
        else if(debounceQuery.trim().length===0)
        {
            setResults(null)
        }
    },[debounceQuery,page])

    const clearResults=useCallback(()=>{
        setQuery('')
        setIsLoading(false)
        setError('')
        setPage(initialPage)
    },[initialPage])

    useEffect(()=>{
        return ()=>{
            if(AbortControllerRef.current)
            {
                AbortControllerRef.current.abort();
            }
        }
    },[])

    return {
        query,
        setQuery,
        results,
        isLoading,
        error,
        page,
        setPage,
        search,
        clearResults
    };
}