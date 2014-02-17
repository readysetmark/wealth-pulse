Wealth Pulse
============

Wealth Pulse is web frontend for a ledger journal file. The ledger journal file is based on the command line [Ledger] journal file format and features double-entry accounting for personal finance tracking.


Objective
---------

Short-term: provide better looking reports and charts via a web frontend.

Long-term: provide better reporting on investments.


Dependencies
------------

Backend
- Clojure 1.5.1
- Instaparse 1.2.12
- Compojure 1.1.6
- Ring-JSON 0.2.0

Frontend
- React 0.8.0
- Underscore 1.6.0
- Backbone 1.1.1 (routing only)
- D3 3.0.0
- jQuery 2.1.0
- jQuery Hotkeys
- Bootstrap 2.3.1


How to Run
----------

TBD

### Development

    lein ring server-headless


Command Bar Supported Commands
------------------------------

You can use the '/' hotkey to reach the command bar.

Commands

    Balance:   bal [accounts-to-include] [parameters]

    Register:  reg [accounts-to-include] [parameters]

    Net Worth: nw


Parameters

    :exclude [accounts-to-exclude]

    :period [this month|last month]

    :since [yyyy/mm/dd]

    :upto [yyyy/mm/dd]

    :title [report title]


Implementation Notes
--------------------

Investments & Commodities:

*	I'm basically ignoring these for the moment. The parser will parse them, but all processing after that point assumes one commodity and basically assumes only the "amount" field is used. I'll need to revisit this once I get around to adding investment/commodity support.


Phase 1 Implementation (Reporting)
----------------------

### Objective

*	Replace the ledger bal and reg commandline options with a web interface.
*	Provide some basic reporting like net worth and income vs expenses
*	See http://bugsplat.info/static/stan-demo-report.html for some examples


### First Milestone

Parsing Ledger File
- [x] Basic / optimistic parsing of ledger file
- [x] Autobalance transactions
- [x] Ensure transactions balance (if not autobalanced)

Balance Report
- [x] Single balance report with parameters (filters) and generates JSON data
  - [x] Balance report query
  - [x] Balance report view

Net Worth Chart
- [x] Report to generate JSON data for a networth chart

Single Page Web App
- [x] Serve static files (css/js)
- [x] "Static" links for balance sheet, net worth, current income, previous income


### Second Milestone

Dynamic Website:
- [x] Setup command bar
- [x] Highlight active page on navlist


### Third Milestone

Register Report
- [ ] Register report with parameters (ie accounts, date range)
	- [ ] build register report generator function
	- [ ] create register report template
	- [ ] link up to command bar
	- [ ] link to from balance reports
- [ ] Sorting:
	- [ ] Preserve file order for transactions and entries within transactions but output in reverse so most recent is on top
		- Need to do sorting at the end so that running total makes sense
- [ ] Accounts Payable vs Accounts Receivable
	- Dynamically list non-zero accounts with balance in navlist. Link to register report


### Fourth Milestone

Command Bar Enhancements
- [ ] Add fault tolerance to parameter parsing
- [ ] Clean up and improve date/period parsing
	Additions for period: yyyy, last year, this year
- [x] Generate "networth" chart from the command bar
- [ ] Autocomplete hints (bootstrap typeahead)

Setup Runnable App
- [ ] Write main
- [ ] Watch ledger file and reload on change
	- [ ] Handle situation where file cannot be parsed

Documentation
- [ ] github wiki
	- [ ] how to use / setup


### Someday/Maybe

- [x] Networth chart hover does not work properly in Firefox
- [ ] Move title/subtitle stuff to frontend?
- [ ] Notification when ajax call is happening?
- [ ] Clean up web.clj
- [ ] Clean up wealthpulse.js
- [ ] Unit tests
- [ ] Speed up parsing with instaparse?
- [ ] Speed up balance query by filtering accounts first?
- [ ] Speed up networth using custom query?


Phase 2 Implementation (Commodities)
----------------------

Commodity Prices
- [ ] Update to handle commodities
- [ ] Detect investment transactions and merge transaction lines (while continuing to use ledger file format)
- [ ] Identify commodities from ledger file
- [ ] Fetch prices from internet and add to cache
	- [ ] Store commodity prices in a local cache
	- [ ] Prices should go from first date in ledger file to today

Net Worth
- [ ] Update chart with book value line and actual line

Balance Sheet
- [ ] Update Net Worth sheet with actual vs book value columns

Portfolio
- [ ] Overall portfolio return and per investment
- [ ] Expected T3s/T5s to receive for last year (ie had distribution)
- [ ] Rebalancing calculator - for rebalancing investments to proper allocation

Expenses
- [ ] Average in last 3 months, in last year
- [ ] Burn rate - using last 3 months expenses average, how long until savings is gone?
- [ ] Top Expenses over last period

Charts
- [ ] Income Statement chart (monthly, over time)

Nav
- [ ] Configurable nav list
- [ ] Combine reports and payables / receivables into one dict?
- [ ] Default report?


[Ledger]: http://www.ledger-cli.org/
