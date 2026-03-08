<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="xml" indent="yes"/>

  <xsl:param name="filterCategory" select="'all'"/>
  <xsl:param name="heading" select="'Book List'"/>

  <xsl:template match="/bookstore">
    <result>
      <heading><xsl:value-of select="$heading"/></heading>
      <xsl:choose>
        <xsl:when test="$filterCategory = 'all'">
          <xsl:for-each select="book">
            <item><xsl:value-of select="title"/></item>
          </xsl:for-each>
        </xsl:when>
        <xsl:otherwise>
          <xsl:for-each select="book[@category=$filterCategory]">
            <item><xsl:value-of select="title"/></item>
          </xsl:for-each>
        </xsl:otherwise>
      </xsl:choose>
    </result>
  </xsl:template>
</xsl:stylesheet>
