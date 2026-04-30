-- 상품명과 설명에 대해 전문 검색 인덱스 생성
ALTER TABLE products ADD FULLTEXT INDEX idx_combined_search (name, description) WITH PARSER ngram;