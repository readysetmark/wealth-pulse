/*****
  Sidebar Navigation
*****/

// ReportNav
//   @className
//   @url
//   @title
var ReportNav = React.createClass({
  render: function() {
    return React.DOM.li({className: this.props.className},
                        React.DOM.a({href: this.props.url}, this.props.title));
  }
});


// PayeeNav
//   @className
//   @url
//   @name
//   @amountClass
//   @amount
var PayeeNav = React.createClass({
  render: function() {
    return React.DOM.li({className: this.props.className},
                        React.DOM.a({href: this.props.url},
                                    this.props.name,
                                    React.DOM.span({className: "pull-right " + this.props.amountClass}, this.props.amount)));
  }
});


// NavBox
var NavBox = React.createClass({
  render: function() {
    var report_nodes = [];
    var payee_nodes = [];
    var i = 0;

    if (this.props.hasOwnProperty('reports')) {
      for (i = 0; i < this.props.reports.length; i++) {
        var report = this.props.reports[i];
        report_nodes.push(ReportNav(report));
      }
    }

    if (this.props.hasOwnProperty('payees')) {
      for (i = 0; i < this.props.payees.length; i++) {
        var payee = this.props.payees[i];
        payee_nodes.push(PayeeNav({className: payee.class,
                                 url: payee.url,
                                 name: payee.name,
                                 amountClass: payee.amountClass,
                                 amount: payee.amount,
                                 key: payee.name}));
      }
    }

    return React.DOM.div({
        className: "well",
        style: {
          padding: "8px 0"
        }
      },
      React.DOM.ul({className: "nav nav-list"},
        React.DOM.li({className: "nav-header"}, "Reports"),
        report_nodes,
        React.DOM.li({className: "nav-header"}, "Payables / Receivables"),
        payee_nodes)
    );
  }
});



/*****
  Balance Report
*****/

// BalanceReportRow
//   @rowClass
//   @balanceClass
//   @balance
//   @accountStyle
//   @account
var BalanceReportRow = React.createClass({
  render: function() {
    var row = React.DOM.tr({className: this.props.rowClass},
                           React.DOM.td({className: "currency "+ this.props.balanceClass}, this.props.balance),
                           React.DOM.td({style: this.props.accountStyle},
                                        React.DOM.a({href: "#/TODO_register_link"}, this.props.account)));
    return row;
  }
});


// BalanceReport
//   @title
//   @subtitle
//   @balances
var BalanceReport = React.createClass({
  render: function() {
    var table_rows = [];
    var i = 0;

    if (this.props.hasOwnProperty('balances')) {
      for (i = 0; i < this.props.balances.length; i++) {
        var balance = this.props.balances[i];
        table_rows.push(BalanceReportRow(balance));
      }
    }

    var header = React.DOM.header({className: "page-header"},
                                  React.DOM.h1(null,
                                               this.props.title,
                                               React.DOM.br(),
                                               React.DOM.small(null, this.props.subtitle)));
    var body = React.DOM.section({className: "span4"},
                                 React.DOM.table({className: "table table-hover table-condensed"},
                                                 React.DOM.thead(null,
                                                                 React.DOM.tr(null,
                                                                              React.DOM.th(null, "Balance"),
                                                                              React.DOM.th(null, "Account"))),
                                                 React.DOM.tbody(null, table_rows)));

    return React.DOM.div(null, header, body);
  }
});



/*****
  Networth Report
*****/

// NetworthReport
//   @title
//   @data
var NetworthReport = React.createClass({
  componentDidMount: function (root) {
    var margin = {top: 20, right: 20, bottom: 30, left: 55},
        width = 600 - margin.left - margin.right,
        height = 400 - margin.top - margin.bottom;

    var parseDate = d3.time.format("%d-%b-%Y").parse;

    var x = d3.time.scale()
    .range([0, width]);

    var y = d3.scale.linear()
    .range([height, 0]);

    var xAxis = d3.svg.axis()
    .scale(x)
    .ticks(d3.time.months, 3)
    .tickFormat(d3.time.format("%b %y"))
    .orient("bottom");

    var yAxis = d3.svg.axis()
    .scale(y)
    .orient("left");

    var line = d3.svg.line()
    .x(function(d) { return x(d.date); })
    .y(function(d) { return y(d.amount); });

    var svg = d3.select('#linechart').append("svg")
    .attr("width", width + margin.left + margin.right)
    .attr("height", height + margin.top + margin.bottom)
    .append("g")
    .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

    if (!this.props.data) {
      return;
    }

    this.props.data.forEach(function(d) {
      d.date = parseDate(d.date);
      d.amount = parseFloat(d.amount);
    });

    x.domain(d3.extent(this.props.data, function(d) { return d.date; }))
    .nice(d3.time.month);
    y.domain(d3.extent(this.props.data, function(d) { return d.amount; }));

    svg.append("g")
    .attr("class", "x axis")
    .attr("transform", "translate(0," + height + ")")
    .call(xAxis);

    svg.append("g")
    .attr("class", "y axis")
    .call(yAxis)
    .append("text")
    .attr("transform", "rotate(-90)")
    .attr("y", 6)
    .attr("dy", ".71em")
    .style("text-anchor", "end")
    .text("Amount ($)");

    svg.append("path")
    .datum(this.props.data)
    .attr("class", "line")
    .attr("d", line);

    var node = svg.append("g")
    .attr("class", "nodes")
    .selectAll("circle")
    .data(this.props.data)
    .enter()
    .append("circle")
    .attr("class", "node")
    .attr("r", 2)
    .attr("cx", function(d) { return x(d.date); })
    .attr("cy", function(d) { return y(d.amount); });

    var hover = svg.append("g")
    .attr("class", "node-hover")
    .style("display", "none");

    var hover_rect = hover.append("rect")
    .attr("class", "node-hover-rect")
    .attr("rx", "5")
    .attr("ry", "5")
    .attr("width", "120")
    .attr("height", "30");

    var hover_text = hover.append("text")
    .attr("class", "node-hover-text")
    .attr("transform", "translate(10,20)");

    node
    .on("mouseover", function(d) {
      var text_width = 120;
      var lines = 1;

      hover_text.selectAll("tspan").remove();
      var name = hover_text.append("tspan")
      .attr("class", "node-hover-text-name")
      .attr("x", "0")
      .text(d.hover);

      text_width = name.node().getComputedTextLength();

      hover_rect
      .attr("width", text_width + 20);

      var translate_x = x(d.date) + 10;
      if (translate_x + text_width + 20 > width) {
        translate_x = x(d.date) - 30 - text_width;
      }

      var translate_y = y(d.amount) + 10;
      if (translate_y + 30 > height) {
        translate_y = y(d.amount) - 40;
      }

      hover
      .attr("transform", "translate(" + translate_x + ", " + translate_y +")")
      .style("display", "block");
    })
    .on("mouseout", function(d) {
      hover.style("display", "none");
    })
  },
  render: function () {
    var header = React.DOM.header({className: "page-header"},
                                  React.DOM.h1(null, this.props.title));

    var body = React.DOM.section({id: 'linechart'});

    return React.DOM.div(null, header, body);
  }
});


/*****
  Routes
*****/

var WealthPulseRouter = Backbone.Router.extend({
  routes: {
    '': 'home',
    'balance': 'balance',
    'balance?*query': 'balance',
    'networth': 'networth'
  }
});



/*****
  App Component
*****/

var WealthPulseApp = React.createClass({
  getInitialState: function() {
    return {navData: {}, report: "", query: "", reportData: {}};
  },
  componentWillMount: function () {
    var that = this;
    this.router = new WealthPulseRouter();
    this.router.on('route:home', this.home);
    this.router.on('route:balance', this.balance);
    this.router.on('route:networth', this.networth);
  },
  componentDidMount: function () {
    Backbone.history.start();
  },

  // Routes
  home: function () {
    var defaultReport = 'balance';
    var defaultQuery = 'accountsWith=assets+liabilities&excludeAccountsWith=units&title=Balance+Sheet';
    console.log('home');
    this.loadData(defaultReport, defaultQuery);
  },
  balance: function (query) {
    console.log('balance with query='+ query);
    this.loadData('balance', query);
  },
  networth: function () {
    console.log('networth');
    this.loadData('networth');
  },

  // Data Fetching
  loadData: function (report, query) {
    var self = this;
    $.when(this.loadNav(), this.loadReport(report, query))
      .done(function (navArgs, reportArgs) {
        console.log("ajax done.");
        self.setState({
          navData: navArgs[0],
          report: report,
          query: query,
          reportData: reportArgs[0]
        });
      });
  },
  loadNav: function () {
    return $.ajax({
      url: 'api/nav',
      dataType: 'json'
    });
  },
  loadReport: function (report, query) {
    return $.ajax({
      url: 'api/' + report + (query ? "?" + query : ""),
      dataType: 'json',
    });
  },

  render: function() {
    var navBox = NavBox(this.state.navData);
    var report;

    console.log("will render: "+ this.state.report);
    if (this.state.report == 'networth') {
      var report = NetworthReport(this.state.reportData);
    }
    else {
      var report = BalanceReport(this.state.reportData);
    }

    var div = React.DOM.div({className: "row-fluid"},
                            React.DOM.nav({className: "span2"}, navBox),
                            React.DOM.section({className: "span10"}, report));

    return div;
  }
});



/*****
  Initialization
*****/

React.renderComponent(
  WealthPulseApp({}),
  document.getElementById('app')
);
