xquery version "1.0";
<titles>
{
for $b in /bookstore/book
order by $b/title
return
  <title>{ string($b/title) }</title>
}
</titles>
