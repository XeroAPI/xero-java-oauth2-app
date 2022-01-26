<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format" >
<xsl:output method="text" omit-xml-declaration="yes" indent="no"/>
<xsl:template match="/">
        Date,Amount,Payee,Description,Reference,Analysis Code
<xsl:for-each select="//Ntry">
<xsl:value-of select="concat(ValDt/Dt,',',Amt,',',Payee,',',AddtlNtryInf,',',AcctSvcrRef,',',CdtDbtInd != 'CRDT','&#xA;')"/>
        <xsl:if test="CdtDbtInd != 'CRDT'">Debit</xsl:if><xsl:value-of select="Amt"/>
</xsl:for-each>
</xsl:template>
</xsl:stylesheet>