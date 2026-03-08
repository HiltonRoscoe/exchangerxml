<?xml version="1.0" encoding="UTF-8"?>
<schema xmlns="http://www.ascc.net/xml/schematron">
  <title>Bookstore Validation Rules</title>
  <pattern name="Book Price Check">
    <rule context="book">
      <assert test="price &gt; 0">Price must be greater than zero.</assert>
      <assert test="title">Every book must have a title.</assert>
      <assert test="author">Every book must have an author.</assert>
    </rule>
  </pattern>
</schema>
