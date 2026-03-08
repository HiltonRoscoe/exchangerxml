<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="xml" indent="yes"/>

  <xsl:template match="/bookstore">
    <html>
      <body>
        <h1>Bookstore</h1>
        <xsl:for-each select="book">
          <p><xsl:value-of select="title"/> by <xsl:value-of select="author"/></p>
        </xsl:for-each>
      </body>
    </html>
  </xsl:template>
</xsl:stylesheet>
