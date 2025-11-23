
import { useSearch } from '../hooks/useSearch'
import SearchBar from '../components/search/SearchBar'

const SearchPage = () => 
{
    const {query,setQuery,results,isLoading,error,clearResults}=useSearch({debounceMs:400,minQueryLength:2,pageSize:10})
  return (
    <div style={{ padding: '40px', maxWidth: '800px', margin: '0 auto' }}>
        <h1>Search bar debug</h1>
        <SearchBar
            value={query}
            onChange={setQuery}
            onClear={clearResults}
            isLoading={isLoading}
            placeholder='Search products'        
        />
        {error && (
        <div style={{ color: 'red', marginTop: '10px' }}>
          <strong>Error:</strong> {error}
        </div>
      )}
      {isLoading && (
        <div style={{ marginTop: '20px' }}>
          <strong>Loading...</strong>
        </div>
      )}
      {results && (
        <pre
          style={{
            marginTop: '20px',
            background: '#1e1e1e',
            color: '#00e676',
            padding: '20px',
            borderRadius: '8px',
            maxHeight: '500px',
            overflow: 'auto',
          }}
        >
        {JSON.stringify(results, null, 2)}
        </pre>
      )}

    </div>
  )

}

export default SearchPage