import { useCallback, useEffect, useRef, useState } from "react"
import type { AutoCompleteResponse, Suggestion } from "../constants"
import { useDebounce } from "./useDebounce"
import { getSuggestion } from "../services/autoCompleteApi";

export interface AutoCompleteOption
{
    debounceMs?: number,
    minQueryLength?: number
}

export interface AutoCompleteReturn
{
    query: string,
    setQuery: (q:string)=>void,
    suggestion:Suggestion[],
    isLoading: boolean,
    error: string|null,
    selectedIndex: number;
    setSelectedIndex: (index: number) => void;
    clearSuggestion:()=>void,
    selectSuggestion:(suggestion:Suggestion)=>void,
    showDropdown: boolean,
    setShowDropdown: (show:boolean)=>void
}

export function useAutoComplete(options:AutoCompleteOption={}):AutoCompleteReturn
{
    const {debounceMs=150,minQueryLength=3}=options

    const [query,setQuery]=useState('')
    const [suggestion,setSuggestion]=useState<Suggestion[]>([])
    const [error, setError] = useState<string | null>(null)
    const [showDropdown,setShowDropdown]=useState(false)
    const [isLoading,setIsLoading]=useState(false)
    const [selectedIndex,setSelectedIndex]=useState(-1)

    const debouncedQuery=useDebounce(query,debounceMs)
    const abortControllerRef=useRef<AbortController|null>(null)


    const fetchSuggestion=useCallback(async(searchQuery:string)=>{
        console.log('🔎 Fetching autocomplete for:', searchQuery);
        if(searchQuery.trim().length<minQueryLength)
        {
            setSuggestion([])
            setShowDropdown(false)
            return
        }
        if(abortControllerRef.current)
        {
            console.log('🛑 Aborting previous autocomplete request');
            abortControllerRef.current.abort()
        }
        abortControllerRef.current=new AbortController()
        setIsLoading(true)
        setError(null)

        try 
        {
            const response :AutoCompleteResponse=await getSuggestion({q:searchQuery.trim()})
            console.log('📥 Autocomplete response:', response);
            setSuggestion(response.suggestions)
            setShowDropdown(response.suggestions.length>0)
            setSelectedIndex(-1)
            
        } 
        catch (err:any) 
        {
            if (err.name !== 'AbortError' && err.name !== 'CanceledError')
            {
                setError(err.message || 'Failed to fetch suggestions');
                setSuggestion([])
                setShowDropdown(false)
            }
            
        }
        finally
        {
            setIsLoading(false)
        }

    },[minQueryLength])

    useEffect(()=>{
        if(debouncedQuery.trim().length>=minQueryLength)
        {
            fetchSuggestion(debouncedQuery)
        }
        else
        {
            setSuggestion([])
            setShowDropdown(false)
        }

    },[debouncedQuery,fetchSuggestion,minQueryLength])

    const clearSuggestion=useCallback(()=>{
        setSuggestion([])
        setShowDropdown(false)
        setSelectedIndex(-1)
        setError(null)
    },[])

    const selectSuggestion=useCallback((suggestion:Suggestion)=>{
        setQuery(suggestion.text)
        clearSuggestion()
    },[clearSuggestion])

     useEffect(() => {
    return () => {
      if (abortControllerRef.current) {
        abortControllerRef.current.abort();
      }
    };
  }, []);

  return {
    query,
    setQuery,
    suggestion,
    isLoading,
    error,
    selectedIndex,
    setSelectedIndex,
    showDropdown,
    setShowDropdown,
    clearSuggestion,
    selectSuggestion

  }

}