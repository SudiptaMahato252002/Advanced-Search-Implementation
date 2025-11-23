import React, { useCallback } from 'react'
import './SearchBar.css';

interface SearchBarProps
{
    value:string,
    onChange: (value:string)=>void,
    onSearch?:()=>void,
    onClear?:()=>void,
    placeholder?: string,
    isLoading?: boolean,
    disabled?: boolean
}

const SearchIcon = () => (
  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
    <circle cx="11" cy="11" r="8" />
    <path d="m21 21-4.35-4.35" />
  </svg>
);

const ClearIcon = () => (
  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
    <line x1="18" y1="6" x2="6" y2="18" />
    <line x1="6" y1="6" x2="18" y2="18" />
  </svg>
);

const LoadingSpinner = () => <div className="search-spinner" aria-hidden="true" />;

const SearchBar = ({value,onChange,onSearch,onClear,placeholder,isLoading,disabled}:SearchBarProps) => {
    const handleInputChange=useCallback((e:React.ChangeEvent<HTMLInputElement>)=>
        {onChange(e.target.value)},[onChange])
    
    const handleKeyDown=useCallback((e:React.KeyboardEvent<HTMLInputElement>)=>{
        if(e.key === 'Enter')
        {
            onSearch?.()
        }
        if(e.key=== 'Escape')
        {
            onClear?.()
        }

    },[onChange,onSearch])

    const handleClear=useCallback(()=>{
        onChange('')
        onClear?.()
    },[onChange,onClear])
  
  
    return (
    <div className='search-bar-cotnainer'>
        <div className='search-bar'>
            <span className='search-icon' aria-hidden='true'><SearchIcon/></span>
            <input 
                type="text"
                value={value}
                onChange={handleInputChange}
                onKeyDown={handleKeyDown}
                placeholder={placeholder}
                disabled={disabled}
                aria-label='Search'
                autoComplete='off'
                />
        </div>
        <div className='search-bar-actions'>
            {isLoading && <LoadingSpinner/>}
            {value && !isLoading &&(
                <button
                    type="button"
                    onClick={handleClear}
                    className="clear-button"
                    aria-label="Clear search"
                >
                    <ClearIcon/>
                </button>
            )}
        </div>
    </div>
  )
}

export default SearchBar