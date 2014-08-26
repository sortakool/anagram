-----------------------------------------------------------------------------------------------------------------------------
-- 1. Provide a query that shows the number of trades per client traded on the 1st of August, 2012.
-----------------------------------------------------------------------------------------------------------------------------
select tmp.CustomerId, tmp.CustomerName, CASE WHEN tmp.NumberOfTrades IS NULL THEN 0 ELSE tmp.NumberOfTrades END
FROM
(
  select c.CustomerId, c.CustomerName,
                                        (
                                          select count(t2.CustomerId)
                                          from TRADES t2
                                          where t2.CustomerId = t.CustomerId
                                          group by t2.CustomerId, t2.TradeDate
                                          having t2.TradeDate = date '2012-08-01'
                                        ) as NumberOfTrades
    from CUSTOMERS c left join Trades t on c.CustomerId = t.CustomerId
    group by c.CustomerId, c.CustomerName, t.CustomerId
) as tmp
order by tmp.CustomerId
;

-----------------------------------------------------------------------------------------------------------------------------
-- 2. Provide a query that shows the trade details of the largest (= biggest quantity) trade done for each client.
--    The result set should show the customer name, instrument name, quantity, price, and trade date.
-----------------------------------------------------------------------------------------------------------------------------

-- this version filters out customers without any trades
select c.CustomerId, c.CustomerName, i.InstrumentName, t.Price, t.TradeDate, t.Quantity
from
CUSTOMERS c join Trades t on c.CustomerId = t.CustomerId
join INSTRUMENTS i on i.InstrumentId = t.InstrumentId
join
(
  select t2.CustomerId, max(abs(t2.Quantity)) as MaxQty from Trades t2 group by t2.CustomerId
) as tmp on (t.CustomerId = tmp.CustomerId and abs(t.Quantity) = (tmp.MaxQty))
order by c.CustomerName
;

--this version shows all customers, but has nulls for instrumentname, price, tradedate and quantity for customers without any trades
select c2.CustomerName, tmp2.instrumentname, tmp2.price, tmp2.tradedate, tmp2.quantity
from Customers c2 left join
(
  select c.CustomerId, c.CustomerName, i.InstrumentName, t.Price, t.TradeDate, t.Quantity
  from
  CUSTOMERS c join Trades t on c.CustomerId = t.CustomerId
  join INSTRUMENTS i on i.InstrumentId = t.InstrumentId
  join
  (
    select t2.CustomerId, max(abs(t2.Quantity)) as MaxQty from Trades t2 group by t2.CustomerId
  ) as tmp on (t.CustomerId = tmp.CustomerId and abs(t.Quantity) = (tmp.MaxQty))
) as tmp2 on c2.CustomerId = tmp2.CustomerId
order by c2.CustomerName
;

-----------------------------------------------------------------------------------------------------------------------------
-- 3. Produce a query that shows the last date a client traded.
--    The result set should show the customer id, customer name, and last trade date (or null if the client has never traded).
-----------------------------------------------------------------------------------------------------------------------------
select c.CustomerId, c.CustomerName, (select max(t2.TradeDate) from Trades t2 where c.CustomerId = t2.CustomerId) as LastTradeDate
from CUSTOMERS c left join Trades t on c.CustomerId = t.CustomerId
group by c.CustomerId, c.CustomerName
order by c.CustomerId
;