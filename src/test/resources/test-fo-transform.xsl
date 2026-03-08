<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:fo="http://www.w3.org/1999/XSL/Format">

  <xsl:output method="xml" indent="yes"/>

  <xsl:template match="/bookstore">
    <fo:root>
      <fo:layout-master-set>
        <fo:simple-page-master master-name="page" page-height="297mm" page-width="210mm">
          <fo:region-body margin="25mm"/>
        </fo:simple-page-master>
      </fo:layout-master-set>
      <fo:page-sequence master-reference="page">
        <fo:flow flow-name="xsl-region-body">
          <fo:block font-size="16pt" font-weight="bold">Bookstore</fo:block>
          <xsl:for-each select="book">
            <fo:block><xsl:value-of select="title"/> by <xsl:value-of select="author"/></fo:block>
          </xsl:for-each>
        </fo:flow>
      </fo:page-sequence>
    </fo:root>
  </xsl:template>
</xsl:stylesheet>
