// frontend/src/pages/SearchPage.tsx

import { useState } from 'react';
import { AutoCompleteInput } from '../components/search/AutoCompleteInput';
import type { Suggestion } from '../constants';
import { useSearch } from '../hooks/useSearch';

const SearchPage = () => {
  const [selectedSuggestion, setSelectedSuggestion] = useState<Suggestion | null>(null);
  const { results, isLoading, error } = useSearch({ debounceMs: 400, minQueryLength: 2, pageSize: 10 });

  const handleSelect = (suggestion: Suggestion) => {
    console.log('Selected suggestion:', suggestion);
    setSelectedSuggestion(suggestion);
    // You can trigger search or navigation based on suggestion type
    // if (suggestion.type === 'product') navigate to product page
    // if (suggestion.type === 'brand') filter by brand
    // if (suggestion.type === 'category') filter by category
  };

  const handleSearch = (query: string) => {
    console.log('Search triggered for:', query);
    // Trigger full search
  };

  return (
    <div style={{ padding: '40px', maxWidth: '800px', margin: '0 auto' }}>
      <h1>Product Search with Autocomplete</h1>
      
      <div style={{ marginTop: '20px' }}>
        <AutoCompleteInput 
          onSelect={handleSelect}
          onSearch={handleSearch}
          placeholder="Search products, brands, categories..."
        />
      </div>

      {selectedSuggestion && (
        <div style={{ 
          marginTop: '20px', 
          padding: '16px', 
          background: '#f0f9ff', 
          borderRadius: '8px',
          border: '1px solid #bae6fd'
        }}>
          <h3>Selected Suggestion</h3>
          <pre style={{ 
            background: '#1e1e1e', 
            color: '#00e676', 
            padding: '16px', 
            borderRadius: '4px',
            overflow: 'auto'
          }}>
            {JSON.stringify(selectedSuggestion, null, 2)}
          </pre>
        </div>
      )}

      {error && (
        <div style={{ color: 'red', marginTop: '10px' }}>
          <strong>Error:</strong> {error}
        </div>
      )}

      {isLoading && (
        <div style={{ marginTop: '20px' }}>
          <strong>Loading search results...</strong>
        </div>
      )}

      {results && (
        <div style={{ marginTop: '20px' }}>
          <h3>Search Results</h3>
          <pre
            style={{
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
        </div>
      )}
    </div>
  );
};

export default SearchPage;