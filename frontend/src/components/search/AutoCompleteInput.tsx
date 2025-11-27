import React, { useEffect, useRef } from 'react';
import { useAutoComplete } from "../../hooks/useAutoComplete";
import type { Suggestion } from '../../constants';
import './Autocomplete.css';


interface AutoCompleteProps {
  onSearch?: (q: string) => void;
  onSelect?: (suggestion: any) => void;
  placeholder?: string;
}

export const AutoCompleteInput = ({ onSearch, onSelect, placeholder='Search products, brands, categories...', }: AutoCompleteProps) => {

  const {query,setQuery,error,isLoading,suggestion,selectedIndex,setSelectedIndex,selectSuggestion,clearSuggestion,showDropdown,setShowDropdown}=useAutoComplete({debounceMs:300,minQueryLength:3})
  
  const handleClick=(s:Suggestion)=>{
    selectSuggestion(s)
    onSelect?.(s)
    setShowDropdown(false)
  }

  const inputRef= useRef<HTMLInputElement|null>(null)
  const dropDownRef=useRef<HTMLDivElement>(null)

  const handleKeyDown=(e:React.KeyboardEvent<HTMLInputElement>)=>{

    if(!showDropdown ||suggestion.length===0)
    {
      if(e.key==='Enter' && query.trim())
      {
        onSearch?.(query)
      }
    }

    switch(e.key)
    {
      case 'ArrowDown':
        e.preventDefault()
        const nextIndex =
          selectedIndex < suggestion.length - 1?selectedIndex + 1: selectedIndex;
        setSelectedIndex(nextIndex);
        break;
      case 'ArrowUp':
        e.preventDefault()
        const previousIndex=
          selectedIndex>0?selectedIndex-1:-1;
        setSelectedIndex(previousIndex)
        break;
      case 'Enter':
        e.preventDefault()
        if(selectedIndex>0&&selectedIndex<=suggestion.length-1)
        {
          const selected=suggestion[selectedIndex]
          handleSelect(selected)
          onSearch?.(query)
        }else if(query.trim())
        {
          onSearch?.(query)
          clearSuggestion()
        }
        break
      case 'Escape':
        e.preventDefault();
        clearSuggestion();
        inputRef.current?.blur();
        break;
      default:
        break;
    }

  }
  const handleSelect=(suggestion:Suggestion)=>{
    selectSuggestion(suggestion)
    onSelect?.(suggestion)
    inputRef.current?.blur()
  }

  useEffect(()=>{
    const handleClickOutside=(event:MouseEvent)=>{
      if((dropDownRef.current&&!dropDownRef.current.contains(event.target as Node))&&
      (inputRef.current&&!inputRef.current.contains(event.target as Node)))
      {
        setShowDropdown(false)
      }
      document.addEventListener('mousedown',handleClickOutside)
      return () => document.removeEventListener('mousedown', handleClickOutside);
    }

  },[setShowDropdown])

  const getSuggestionIcon = (type: string) => {
    switch (type) {
      case 'product':
        return '📦';
      case 'brand':
        return '🏷️';
      case 'category':
        return '📁';
      default:
        return '🔍';
    }
  };

  return (
    <div className='autocomplete-container'>
      <div className='autocomplete-input-wrapper'>
        <input
        ref={inputRef}
        type="text"
        placeholder={placeholder}
        className='autoComplete-input'
        onChange={(e) => setQuery(e.target.value)}
        onKeyDown={handleKeyDown}
        onFocus={() => suggestion.length > 0 && setShowDropdown(true)}
        autoComplete='off'
        />

        {isLoading&&(<span className='loadig-spinner'>⏳</span>)}
      </div>

      {showDropdown&&suggestion.length>0&&(
        <div className='autocomplete-dropdown' ref={dropDownRef}>
          {suggestion.map((suggestion,i)=>(
            <div className={`suggestion-item ${i === selectedIndex ? 'selected' : ''}`} key={`${suggestion.type}-${suggestion.id}`} onClick={()=>handleClick(suggestion)} onMouseEnter={()=>setSelectedIndex(i)}>
               <span className="suggestion-icon">
                {getSuggestionIcon(suggestion.type)}
              </span>
              <div className="suggestion-content">
                <div className="suggestion-text">{suggestion.text}</div>
                <div className="suggestion-meta">
                  <span className="suggestion-type">{suggestion.type}</span>
                  {suggestion.brand && (
                    <span className="suggestion-brand"> • {suggestion.brand}</span>
                  )}
                  {suggestion.price && (
                    <span className="suggestion-price">
                      {' '}
                      • ₹{suggestion.price.toFixed(2)}
                    </span>
                  )}
                </div>
              </div>
            </div>
          ))}
          {showDropdown && suggestion.length === 0 && query.length >= 3 && !isLoading && (
        <div ref={dropDownRef} className="autocomplete-dropdown">
          <div className="no-results">No suggestions found</div>
        </div>
      )}

        </div>)}

      

    </div>
  );
};
