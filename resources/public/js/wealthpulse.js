/* @jsx React.DOM

TODO:

- NavBox
  - Report
      @class
      @url
      @title
  - Payee
      @class
      @url
      @name
      @amount_class
      @amount

- Balance
    @title
    @subtitle

  - BalanceRow
      @rowclass
      @balanceclass
      @balance
      @accountstyle
      @account

*/

var reports = [
  {title: "Balance Sheet", url: "#/balance?parameters=assets liabilities :exclude units :title Balance Sheet"},
  {title: "Net Worth", url: "#/networth"},
  {title: "Income Statement - Current Month", url: "#/balance?parameters=income expenses :period this month :title Income Statement"},
  {title: "Income Statement - Previous Month", url: "#/balance?parameters=income expenses :period last month :title Income Statement"}
];


var Report = React.createClass({
  render: function() {
    return React.DOM.li(null,
                        React.DOM.a({href: this.props.url}, this.props.title));
  }
});


var NavBox = React.createClass({
  render: function() {
    var report_nodes = [];
    var payee_nodes = [];
    var i = 0;

    for (i = 0; i < this.props.reports.length; i++) {
      var report = this.props.reports[i];
      report_nodes.push(Report({url: report.url,
                                title: report.title}));
    }

    var top = React.DOM.div({
      className: "well",
      style: {
        padding: "8px 0"
      }
    },
    React.DOM.ul({className: "nav nav-list"},
      React.DOM.li({className: "nav-header"}, "Reports"),
      report_nodes,
      React.DOM.li({className: "nav-header"}, "Payables / Receivables"),
      payee_nodes
      )
    );
    return top;
  }
});