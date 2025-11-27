import { useEffect, useState } from "react";

export function useDebounce<T>(value:T,delay:number=300):T
{
    const [debounceValue,setDebounceValue]=useState<T>(value);

    useEffect(()=>{
        console.log("⏳ Debounce Timer Started for:", value);

        const timer=setTimeout(()=>{
            setDebounceValue(value)
        },delay)

        return ()=>{
             console.log("❌ Timer Cleared (Value changed before delay finished)");
            clearTimeout(timer);
        }}
    ,[value,delay])

    return debounceValue;

}